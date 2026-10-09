package com.hscmt.simulation.schedule.controller;

import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.enumeration.JobTargetType;
import com.hscmt.simulation.collect.service.CollectTagService;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.simulation.dataset.service.MeasureDatasetService;
import com.hscmt.simulation.job.ScheduleJobRequest;
import com.hscmt.simulation.layer.dto.LayerUpsertRequest;
import com.hscmt.simulation.layer.service.LayerManageService;
import com.hscmt.simulation.program.service.ProgramExecuteService;
import com.hscmt.simulation.schedule.service.SchedulerManagedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
@UncheckedJwtToken
public class InternalRequestController {

    private final SchedulerManagedService scheduleService;
    private final CollectTagService collectTagService;
    private final MeasureDatasetService measureDatasetService;
    private final ProgramExecuteService programExecuteService;
    private final LayerManageService layerManageService;

    @PostMapping("/job")
    public void upsertJob (@RequestBody ScheduleJobRequest request) {
        /* 스케줄 수정 */
        scheduleService.upsert(
                request.targetId(),
                request.group().name(),
                request.firstExecDttm(),
                request.cycleCd(),
                request.interval()
        );
    }

    @DeleteMapping("/job/{group}/{targetId}")
    public void deleteJob (@PathVariable(name = "group") JobTargetType group, @PathVariable(name = "targetId") String targetId) {
        /* 스케줄 삭제 */
        scheduleService.delete(group.name(), targetId);
    }

    @PostMapping("/collect/{dsId}")
    public void collectTagData (@PathVariable(name = "dsId") String dsId) {
        /* 데이터 수집 */
        collectTagService.collectTagData(dsId);
    }

    @PostMapping("/dataset/{dsId}")
    public void createDatasetFile(@PathVariable(name = "dsId") String dsId) {
        /* 데이터셋 파일 생성 */
        measureDatasetService.createMeasureDatasetFile(dsId);
    }

    @PostMapping("/program-run/{pgmId}")
    public void firstProgramRun (@PathVariable (name = "pgmId") String pgmId) {
        /* 프로그램 최초 실행 */
        programExecuteService.executeProgram(pgmId, ExecutionType.MANUAL);
    }

    @PostMapping("/layer-manage")
    public void saveLayerFileToDb (@RequestBody LayerUpsertRequest request) {
        /* 레이어 파일 read to db write */
        layerManageService.migrateShpToDb(request);
    }
}
