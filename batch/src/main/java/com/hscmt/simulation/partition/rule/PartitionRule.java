package com.hscmt.simulation.partition.rule;

import com.hscmt.simulation.partition.spec.type.PartitionStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.temporal.ChronoUnit;

@NoArgsConstructor
@Data
@SuperBuilder
@AllArgsConstructor
public class PartitionRule {
    private String tableName;
    private PartitionStrategy strategy;
    private ChronoUnit dataStoredPeriodUnit;
    private Integer dataStoredPeriod;
}
