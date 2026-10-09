package com.hscmt.simulation.collect.job.manual.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscmt.common.util.PartitionerUtil;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@StepScope
@Slf4j
public class TagManualCollectPartition implements Partitioner {

    @Value("#{jobParameters['tempFilePath']}")
    private String tempFilePath;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> partitions = new HashMap<>();

        List<TagCollectDto> targets = getTagCollectInfo ();

        /* 파티션크기 설정 */
        int actualGridSize = PartitionerUtil.getActualGridSize(gridSize, targets.size());
        int partitionSize = PartitionerUtil.getPartitionSize(actualGridSize, targets.size());

        /* 파티션할당 */
        PartitionerUtil.splitPartition(partitions, actualGridSize, partitionSize, targets);

        return partitions;
    }

    private List<TagCollectDto> getTagCollectInfo () {
        ObjectMapper om = new ObjectMapper();
        List<TagCollectDto> list;
        try (Stream<String> lines = Files.lines(Paths.get(tempFilePath), StandardCharsets.UTF_8)) {

            list = lines
                    .filter(s -> !s.isBlank())
                    .map(s -> {
                        try {
                            return om.readValue(s, TagCollectDto.class);
                        } catch (IOException e) {
                            log.error("convert temp file line to dto error : {} ", e.getMessage());
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("read temp file error : {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return list;
    }
}
