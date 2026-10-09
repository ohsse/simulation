package com.hscmt.simulation.program.controller;

import com.github.f4b6a3.uuid.UuidCreator;
import com.hscmt.common.controller.CommonController;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.ProcessErrorCode;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.program.dto.PgmExecHistDeleteDto;
import com.hscmt.simulation.program.service.ProgramExecHistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProgramExecHistController extends CommonController {
    private final ProgramExecHistService programExecHistService;

    private final JobLauncher jobLauncher;
    @Qualifier("pgmExecHistCleanupJob")
    private final Job cleanJob;

    @Operation(summary = "프로그램 이력삭제", description = "프로그램 실행이력 및 실행결과 파일 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @DeleteMapping("/pgm/hist/{histId}")
    public ResponseEntity<ResponseObject<Void>> programExecHistClear (@PathVariable(name = "histId") String histId) {
        programExecHistService.deleteByHistId(histId);
        return getResponseEntity();
    }

    @Operation(summary = "프로그램 이력삭제", description = "특정프로그램들의 특정기간 실행이력 및 파일 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/pgm/hists-clear")
    public ResponseEntity<ResponseObject<Void>> clearPgmExecHists(@RequestBody PgmExecHistDeleteDto pgmExecHistDeleteDto) {
        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile(UuidCreator.getTimeOrderedEpoch() + "_" + "pgmIds",".txt");
            try (BufferedWriter writer = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String pgmId : pgmExecHistDeleteDto.getPgmIds()) {
                    writer.write(pgmId);
                    writer.newLine();
                }
            }

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("tempFilePath", tempFilePath.toAbsolutePath().toString())
                    .addJobParameter("startDttm", pgmExecHistDeleteDto.getStartDttm(), LocalDateTime.class)
                    .addJobParameter("endDttm", pgmExecHistDeleteDto.getEndDttm(), LocalDateTime.class)
                    .addJobParameter("fireTime", LocalDateTime.now(), LocalDateTime.class)
                    .toJobParameters();

            try {
                jobLauncher.run(cleanJob, jobParameters);
            } catch (Exception e) {
                log.error("delete pgm exec history error : {}", e.getMessage());
                throw new RestApiException(ProcessErrorCode.RUN_PROCESS_ERROR);
            }

        } catch (IOException e) {
            log.error("create temp file error : {}", e.getMessage());
            throw new RuntimeException(e);
        }



        return getResponseEntity();
    }
}
