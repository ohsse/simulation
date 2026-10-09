package com.hscmt.simulation.web;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.collect.service.CollectTagService;
import com.hscmt.simulation.common.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "02. 수동 Job 태스크 제어", description = "사용자 수동 요청으로 인한 Job Tasklet 제어 서비스 제공")
public class ManualJobTaskController extends CommonController {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("partitionManageJob")
    private final Job partitionManageJob;
    private final CollectTagService collectTagService;

    @Operation(summary = "계측데이터셋 재수집", description = "계측데이터셋 재수집")
    @ApiResponses({
            @ApiResponse(description = "실행", responseCode = "200")
    })
    @GetMapping("/collect/dataset/{dsId}")
    public ResponseEntity<ResponseObject<Void>> collectTagData (@PathVariable(name = "dsId") String dsId) {
        collectTagService.collectTagData(dsId);
        return getResponseEntity();
    }

    @Operation(summary = "파티션확인", description = "파티션 생성여부 확인 및 최신 파티션까지 생성하는 Task")
    @ApiResponses({
            @ApiResponse(description = "실행", responseCode = "200")
    })
    @PostMapping("/check/partition")
    public ResponseEntity<ResponseObject<Long>> partitionChk () {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("jobExecutor", jwtTokenProvider.getSubject())
                .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                .toJobParameters();
        Long jobId;
        try {
            jobId = waitForExecutionId("tagManualCollectJob", jobParameters, 3000);
            jobLauncher.run(partitionManageJob, jobParameters);
        } catch (Exception e) {
            log.error("partitionChk error : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return getResponseEntity(jobId);
    }

    private Long waitForExecutionId(String jobName, JobParameters params, long waitMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + waitMs;
        while (System.currentTimeMillis() < deadline) {
            List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(jobName, 0, 10);
            for (JobInstance ji : instances) {
                // 파라미터 매칭되는 최신 실행 찾기
                List<JobExecution> execs = jobExplorer.getJobExecutions(ji);
                for (JobExecution je : execs) {
                    if (params.equals(je.getJobParameters())) {
                        return je.getId();
                    }
                }
            }
            Thread.sleep(50);
        }
        // 못 찾았으면 null 반환(혹은 예외)
        return null;
    }
}
