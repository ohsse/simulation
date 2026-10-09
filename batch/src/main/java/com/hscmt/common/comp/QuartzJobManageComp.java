package com.hscmt.common.comp;

import com.hscmt.common.QuartzJobErrorCode;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.simulation.dataset.job.MeasureDatasetCreatorJob;
import com.hscmt.simulation.program.job.RealtimeProgramExecuteJob;
import com.hscmt.simulation.schedule.spec.JobSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzJobManageComp {
    @Value("${batch.timezone}")
    private String timezoneId;
    public final String SIGNATURE_KEY = "signature";
    public final String JOB_ID_PREFIX = "job:";
    public final String TRG_KEY_SUFFIX = ":trg";
    @Getter private final Scheduler scheduler;

    /* job 등록 */
    public void registerJob (String targetId, String group, String signature, LocalDateTime firstStartDateTime, CycleCd cycle, Integer interval) {
        JobDetail jobDetail = getJobInfo(targetId, group, signature);
        Trigger trigger = getTriggerInfo(targetId, group, firstStartDateTime, cycle, interval);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            registerErrorLog(e);
        }
    }

    public void registerErrorLog ( Exception e ) {
        log.error("job register error : {}", e.getMessage());
        throw new RestApiException(QuartzJobErrorCode.REGISTER_JOB_ERROR);
    }

    /* job spec 에 따른 내부 job */
    public void upsertJob (JobSpec spec) {

        if (spec.getJobClass() == null) return;

        JobKey jobKey = getJobKey(spec.getKey(), spec.getGroup().name());
        TriggerKey triggerKey = getTriggerKey(spec.getKey(), spec.getGroup().name());

        Map<String, Object > specJobDataMap = spec.getJobData();
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(specJobDataMap);

        JobDetail jobDetail = JobBuilder.newJob(spec.getJobClass())
                .withIdentity(jobKey)
                .usingJobData(SIGNATURE_KEY, spec.getCron())
                .storeDurably(true)
                .requestRecovery(true)
                .setJobData(jobDataMap)
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(spec.getCron())
                .inTimeZone(TimeZone.getTimeZone(timezoneId));

        switch (spec.getMisfire()) {
            case DO_NOTHING -> scheduleBuilder.withMisfireHandlingInstructionDoNothing();
            case FIRE_AND_PROCEED -> scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
            case IGNORE_MISFIRES -> scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobDetail)
                .withSchedule(scheduleBuilder)
                .build();

        if (!isExistJob(jobKey)) {
            try {
                scheduler.addJob(jobDetail, true);
                scheduler.scheduleJob(trigger);
                return;
            } catch (SchedulerException e) {
                registerErrorLog(e);
            }
        }

        if (isChanged(spec.getKey(), spec.getGroup().name(), spec.getCron())) {
            try {
                if (isExistTrigger(triggerKey)) {
                    scheduler.rescheduleJob(triggerKey, trigger);
                } else {
                    scheduler.scheduleJob(trigger);
                }
            } catch (SchedulerException e) {
                registerErrorLog(e);
            }
        }
    }


    /* job 수정 */
    public void updateJob (String targetId, String group, String signature, LocalDateTime firstStartDateTime, CycleCd cycle, Integer interval) {
        JobKey jobKey = getJobKey(targetId, group);
        TriggerKey triggerKey = getTriggerKey(targetId, group);
        try {
            JobDetail existJobDetail = scheduler.getJobDetail(jobKey);
            if (existJobDetail == null) {
                registerJob(targetId, group, signature, firstStartDateTime, cycle, interval);
            }else {
                JobBuilder jb = existJobDetail.getJobBuilder();
                JobDetail updated = jb.usingJobData(SIGNATURE_KEY, signature).build();
                scheduler.addJob(updated, true);

                Trigger newTrigger = getTriggerInfo(targetId, group, firstStartDateTime, cycle, interval);
                if (isExistTrigger(triggerKey)) {
                    scheduler.rescheduleJob(triggerKey, newTrigger);
                } else {
                    scheduler.scheduleJob(newTrigger);
                }
            }
        } catch (Exception e) {
            log.error("job update error : {}", e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.UPDATE_JOB_ERROR);
        }
    }

    /* job key 목록 가져오기 */
    public Set<JobKey> getJobKeysByGroup (String group) {
        try {
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
        } catch (Exception e) {
            log.error( "job grouping error : {}" , e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.GROUPING_JOB_ERROR);
        }
    }

    /* jobKey 발급 */
    public JobKey getJobKey (String targetId, String group) {
        return JobKey.jobKey(JOB_ID_PREFIX + targetId, group);
    }
    
    /* 트리거 id 발급 */
    public TriggerKey getTriggerKey (String targetId, String group) {
        return TriggerKey.triggerKey(getJobKey(targetId, group).getName() + TRG_KEY_SUFFIX, group);
    }

    /* targetId로 job 삭제 */
    public void deleteById ( String targetId, String group ) {
        TriggerKey triggerKey = getTriggerKey(targetId, group);
        JobKey jobkey = getJobKey(targetId, group);
        try {
            /* 트리거를 job에서 제거 */
            if (isExistTrigger(triggerKey)) unscheduled(triggerKey);
            /* job 삭제 */
            if (isExistJob(jobkey)) deleteJob(jobkey);
        } catch (SchedulerException e) {
            log.error("delete job Error : {}", e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.DELETE_JOB_ERROR);
        }
    }

    /* job 만들기 */
    public JobDetail getJobInfo (String targetId, String group, String signature) {

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SIGNATURE_KEY, signature);
        jobDataMap.put("id", targetId);

        Class<? extends Job> jobClass = null;
        /* job 이 program 관련이면 */
        if (JobTargetType.PROGRAM.name().equals(group)) {
            jobClass = RealtimeProgramExecuteJob.class;
        } else if (JobTargetType.DATASET.name().equals(group)) {
            jobClass = MeasureDatasetCreatorJob.class;
        }

        return JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(targetId, group))
                .setJobData(jobDataMap)
                .storeDurably(true)
                .requestRecovery(true)
                .build();
    }

    /* 트리거 만들기 */
    public Trigger getTriggerInfo (String targetId, String group, LocalDateTime firstStartDttm, CycleCd cycle, Integer interval) {
        /* 최초시작시간 offset */
        OffsetDateTime start = firstStartDttm.atZone(ZoneId.of(timezoneId)).toOffsetDateTime();
        /* 간격 */
        int triggerInterval = interval <= 0 ? 1 : interval;

        /* 트리거 생성 */
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(targetId, group))
                .forJob(getJobKey(targetId, group))
                .startAt(Date.from(start.toInstant()));

        CalendarIntervalScheduleBuilder cib = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                .inTimeZone(TimeZone.getTimeZone(timezoneId))
                .withMisfireHandlingInstructionFireAndProceed();

        ScheduleBuilder<?> schedule = switch (cycle) {
            case MIN -> cib.withIntervalInMinutes(triggerInterval);
            case HOUR -> cib.withIntervalInHours(triggerInterval);
            case DAY -> cib.withIntervalInDays(triggerInterval);
            case MON -> cib.withIntervalInMonths(triggerInterval);
            case YEAR -> cib.withIntervalInYears(triggerInterval);
        };

        return triggerBuilder.withSchedule(schedule).build();

    }

    /* job 관련 meta변경여부확인 */
    public boolean isChanged (String targetId, String group, String signature) {
        JobKey jobkey = getJobKey(targetId, group);
        if (!isExistJob(jobkey)) return true;
        JobDetail jobDetail = null;
        try {
            jobDetail = scheduler.getJobDetail(jobkey);
        } catch (SchedulerException e) {
            log.error("isChanged Error : {}", e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.CHECK_JOB_ERROR);
        }
        String oldSignature = jobDetail.getJobDataMap().getString(SIGNATURE_KEY);
        return !Objects.equals(oldSignature, signature);
    }

    /* 간단 서명 작성 */
    public String getSignature (CycleCd cycle, Integer interval, LocalDateTime firstStartDttm) {
        String raw = "%s|%d|%s".formatted(
                cycle.name(),
                interval,
                firstStartDttm.atZone(ZoneId.of(timezoneId)).toOffsetDateTime()
        );
        return Integer.toHexString(raw.hashCode());
    }


    /* job 이 메터데이터에 존재하는지 확인 */
    public boolean isExistJob (JobKey jobkey) {
        try {
            return scheduler.checkExists(jobkey);
        } catch (SchedulerException e) {
            log.error("isExistJob Error : {}", e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.CHECK_JOB_ERROR);
        }
    }
    
    /* 트리거가 메터데이터에 존재하는지 확인 */
    public boolean isExistTrigger (TriggerKey triggerKey) {
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            log.error("isExistTrigger Error : {}", e.getMessage());
            throw new RestApiException(QuartzJobErrorCode.CHECK_TRIGGER_ERROR);
        }
    }

    /* trigger 해제 */
    private void unscheduled (TriggerKey triggerKey) throws SchedulerException {
        scheduler.unscheduleJob(triggerKey);
    }

    /* job삭제 */
    private void deleteJob (JobKey jobKey) throws SchedulerException {
        scheduler.deleteJob(jobKey);
    }

    /* group으로 job id Set 찾기 */
    public Set<String> getJobIdSet (String group) {
        return getJobKeysByGroup(group)
                .stream()
                .map (jk -> {
                    String name = jk.getName();
                    String prefix = JOB_ID_PREFIX;
                    if (!name.startsWith(prefix)) return name;
                    return name.substring(prefix.length());
                })
                .collect(Collectors.toSet());
    }
}

