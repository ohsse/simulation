package com.hscmt.simulation.partition.rule;

import com.hscmt.simulation.partition.rule.type.HashPartition;
import com.hscmt.simulation.partition.rule.type.RangePartition;
import com.hscmt.simulation.partition.spec.type.PartitionRangeType;
import com.hscmt.simulation.partition.spec.type.RangeFieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper=true)
@Data
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class RangeHashRule extends PartitionRule implements RangePartition, HashPartition {
    private String rangeField;
    private PartitionRangeType partitionRangeType;
    private String hashField;
    private Integer hashCount;
    private RangeFieldType rangeFieldType;

    public String getDetachPartitionTableName( ) {
        PartitionRangeType rangeType = getPartitionRangeType();
        LocalDateTime targetDateTime =  LocalDateTime.now().minus(getDataStoredPeriod(), getDataStoredPeriodUnit())
                .minus(rangeType.getPeriod(), rangeType.getUnit());
        return getPartitionTableName(getTableName(), partitionRangeType, targetDateTime);
    }

    private String getPartitionTableName (String tableName, PartitionRangeType rangeType, LocalDateTime targetDateTime) {
        return new StringBuffer(tableName)
                .append("_p")
                .append(rangeType.getRangeName(targetDateTime))
                .toString();
    }
}
