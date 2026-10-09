package com.hscmt.simulation.partition.rule.type;

import com.hscmt.simulation.partition.spec.type.PartitionRangeType;
import com.hscmt.simulation.partition.spec.type.RangeFieldType;

public interface RangePartition {
    String getRangeField();
    PartitionRangeType getPartitionRangeType();
    RangeFieldType getRangeFieldType();
}