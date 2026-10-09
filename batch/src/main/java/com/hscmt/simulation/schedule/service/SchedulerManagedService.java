package com.hscmt.simulation.schedule.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hscmt.common.QuartzJobErrorCode;
import com.hscmt.common.comp.QuartzJobManageComp;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.simulation.dataset.job.MeasureDatasetCreatorJob;
import com.hscmt.simulation.schedule.spec.BuiltinJobs;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.repository.MeasureDatasetRepository;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.hscmt.simulation.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerManagedService {
    private final QuartzJobManageComp quartzJobManageComp;

    private final ProgramRepository programRepository;
    private final MeasureDatasetRepository repository;

    /* 서버기동시 job reconcile */
    public void reconcileJobs () {
        /* 프로그램 job reconcile */
        reconcileProgramJobs();
        /* 실시간 데이터셋 job reconcile */
        reconcileRealtimeDatasetJobs();
        /* 고정데이터셋 job reconcile */
        reconcileFixedDatasetJobs();
        /* 고정스케줄 job reconcile */
        reconcileStaticJobs();
    }

    private void reconcileStaticJobs () {
        final String group = JobTargetType.STATIC.name();

        Set<String> jobKeys = quartzJobManageComp.getJobIdSet(group);

        for (BuiltinJobs spec : BuiltinJobs.values()) {
            quartzJobManageComp.upsertJob(spec);
            jobKeys.remove(spec.getKey());
        }

        removeJobBySet(jobKeys, group);
    }

    /* 고정 데이터셋 초기화 */
    private void reconcileFixedDatasetJobs () {
        /* 한번만 트리거 하고 말것으로 임시 그룹으로 세팅 */
        final String group = JobTargetType.TEMP.name();
        List<MeasureDatasetDto> targets = repository.findAllDatasets(YesOrNo.N);
        final String prefix = "temp:";
        for (MeasureDatasetDto d : targets) {
            Scheduler scheduler = quartzJobManageComp.getScheduler();

            String jobKey = prefix + d.getDsId() + UuidCreator.getTimeOrderedEpoch();
            String triggerKey = jobKey + quartzJobManageComp.TRG_KEY_SUFFIX;
            String targetId = d.getDsId();

            JobDetail tempJob = JobBuilder.newJob(MeasureDatasetCreatorJob.class)
                    .withIdentity(JobKey.jobKey(jobKey,group))
                    .storeDurably(false)
                    .requestRecovery(false)
                    .usingJobData("id", targetId )
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(TriggerKey.triggerKey(triggerKey,group))
                    .forJob(tempJob)
                    .startNow()
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withRepeatCount(0)
                                    .withMisfireHandlingInstructionFireNow()
                    )
                    .build();

            try {
                scheduler.scheduleJob(tempJob, trigger);
            } catch (SchedulerException e) {
                log.error("fixed dataset job schedule error : {}", e.getMessage());
                throw new RestApiException(QuartzJobErrorCode.REGISTER_JOB_ERROR);
            }
        }
    }

    /* 실시간 데이터셋 job reconcile */
    private void reconcileRealtimeDatasetJobs () {
        final String group = JobTargetType.DATASET.name();

        List<MeasureDatasetDto> targetDatasets = repository.findAllDatasets(YesOrNo.Y);
        /* job key 에서 프리픽스 값을 빼고, 프로그램 ID만 발췌 */
        Set<String> remainingIds = quartzJobManageComp.getJobIdSet(group);

        for (MeasureDatasetDto d : targetDatasets) {
            String targetId = d.getDsId();
            LocalDateTime firstStartDttm = d.getRgstDttm();
            CycleCd cycle = d.getTermTypeCd();
            upsert (targetId, group, firstStartDttm, cycle, 1);

            remainingIds.remove(d.getDsId());
        }

        removeJobBySet(remainingIds, group);
    }

    /* 실시간 프로그램 job reconcile */
    private void reconcileProgramJobs () {
        final String group = JobTargetType.PROGRAM.name();
        /* job 등록대상 프로그램 조회 */
        List<ProgramDto> targetPrograms = programRepository.findAllRealtimePrograms();

        /* job key 에서 프리픽스 값을 빼고, 프로그램 ID만 발췌 */
        Set<String> remainingIds = quartzJobManageComp.getJobIdSet(group);

        /* 실시간 프로그램 순회 하면서 기존 id랑 비교 */
        for (ProgramDto p : targetPrograms) {
            String targetId = p.getPgmId();
            LocalDateTime firstStartDttm = p.getStrtExecDttm();
            CycleCd cycle = p.getRpttIntvTypeCd();
            Integer interval = p.getRpttIntvVal();
            /* 추가 및 수정 */
            upsert(targetId, group, firstStartDttm, cycle, interval);
            /* 기존 ID 에서 실시간 프로그램 ID 제거 */
            remainingIds.remove(p.getPgmId());
        }

        removeJobBySet(remainingIds, group);
    }

    protected void removeJobBySet (Set<String> deleteIds, String group) {
        /* 남은 ID 는 현재 실시간 ID 가 아니므로 job 에서 삭제 */
        for (String targetId : deleteIds) {
            delete(targetId, group);
        }
    }


    /* job 등록 및 수정 */
    public void upsert (String targetId, String group, LocalDateTime firstStartDttm, CycleCd cycle, Integer interval) {
        String signature = quartzJobManageComp.getSignature(cycle, interval, firstStartDttm);
        if (!quartzJobManageComp.isExistJob(quartzJobManageComp.getJobKey(targetId, group))) {
            quartzJobManageComp.registerJob(targetId, group, signature, firstStartDttm, cycle, interval);
        } else if (quartzJobManageComp.isChanged(targetId, group, signature)) {
            quartzJobManageComp.updateJob(targetId, group, signature, firstStartDttm, cycle, interval);
        }
    }

    /* job 삭제 */
    public void delete (String targetId, String group) {
        quartzJobManageComp.deleteById(targetId, group);
    }



}
