package com.hscmt.simulation.partition.job.config.task;

import com.hscmt.simulation.partition.service.PartitionService;
import com.hscmt.simulation.partition.spec.PartitionTable;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@StepScope
public class PartitionTableDropTasklet implements Tasklet {
    @Value("#{jobParameters['targetTableName']}")
    private String targetTableName;

    private final PartitionService partitionService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        if (targetTableName == null || targetTableName.isEmpty()) {
            List<PartitionTable> tables = Arrays.stream(PartitionTable.values()).toList();

            for (PartitionTable table : tables) {
                partitionService.dropPartitionTable(table);
            }
        } else {
            PartitionTable table = PartitionTable.valueOf(targetTableName);
            partitionService.dropPartitionTable(table);
        }

        return RepeatStatus.FINISHED;
    }
}
