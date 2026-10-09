package com.hscmt.simulation.collect.job.manual.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@StepScope
@Slf4j
public class ClearTempFileTask implements Tasklet {
    @Value("#{jobParameters['tempFilePath']}")
    private String tempFilePath;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        if (tempFilePath == null || tempFilePath.isBlank()) {
            log.warn("tempFilePath is null or blank. skip clear");
            return RepeatStatus.FINISHED;
        }

        Path path = Paths.get(tempFilePath);
        try {
            if (Files.exists(path)) {
                boolean deleted = Files.deleteIfExists(path);
                if (deleted) {
                    log.info("temp file delete success : {}", tempFilePath);
                } else {
                    log.info("temp file not found : {}", tempFilePath);
                }
            }
        } catch (Exception e) {
            log.error("temp file delete error : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return RepeatStatus.FINISHED;
    }
}
