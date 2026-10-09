package com.hscmt.simulation.partition.spec;

import com.hscmt.simulation.partition.rule.PartitionRule;
import com.hscmt.simulation.partition.rule.RangeHashRule;
import com.hscmt.simulation.partition.spec.type.PartitionRangeType;
import com.hscmt.simulation.partition.spec.type.PartitionStrategy;
import com.hscmt.simulation.partition.spec.type.RangeFieldType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
public enum PartitionTable {
    MSRM_L (
            RangeHashRule.builder()
                    .strategy(PartitionStrategy.RANGE_HASH)
                    .tableName("msrm_l")
                    .dataStoredPeriodUnit(ChronoUnit.YEARS)
                    .dataStoredPeriod(3)
                    .rangeField("msrm_dttm")
                    .rangeFieldType(RangeFieldType.TIMESTAMP)
                    .partitionRangeType(PartitionRangeType.MONTHLY)
                    .hashField("tag_sn")
                    .hashCount(8)
                    .build()
    ),
    PGM_EXEC_H (
            RangeHashRule.builder()
                    .strategy(PartitionStrategy.RANGE_HASH)
                    .tableName("pgm_exec_h")
                    .dataStoredPeriodUnit(ChronoUnit.YEARS)
                    .dataStoredPeriod(1)
                    .rangeField("exec_strt_dttm")
                    .rangeFieldType(RangeFieldType.TIMESTAMP)
                    .partitionRangeType(PartitionRangeType.MONTHLY)
                    .hashField("pgm_id")
                    .hashCount(8)
                    .build()
    )
    ;

    private final PartitionRule rule;
}
