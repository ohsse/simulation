package com.hscmt.simulation.program.job.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscmt.common.util.PartitionerUtil;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@StepScope
@RequiredArgsConstructor
public class ProgramExecHistCleanupPartitioner implements Partitioner {

    @Value("#{jobParameters['tempFilePath']}")
    private String tempFilePath;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partition = new HashMap<>();

        List<String> targetIds = getPgmIds();

        int actualGridSize = PartitionerUtil.getActualGridSize(gridSize, targetIds.size());
        int partitionSize = PartitionerUtil.getPartitionSize(actualGridSize, targetIds.size());

        PartitionerUtil.splitPartition(partition, actualGridSize, partitionSize, targetIds);

        return partition;
    }

    private List<String> getPgmIds () {
       List<String> result = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Paths.get(tempFilePath), StandardCharsets.UTF_8)) {
           result = lines.filter(x -> !x.isBlank()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
