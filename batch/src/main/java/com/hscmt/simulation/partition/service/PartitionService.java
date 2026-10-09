package com.hscmt.simulation.partition.service;

import com.hscmt.simulation.partition.rule.RangeHashRule;
import com.hscmt.simulation.partition.spec.PartitionTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartitionService {

    private final RangeHashPartitionManager rangeHashPartitionManager;

    public void createPartitionTable(PartitionTable partitionTable) {
        /* 현재 레인지 파티션 기준으로 개발되어 있음. TO DO 추가 적으로 나주엥 하자 */
        if (partitionTable.getRule() instanceof RangeHashRule rhr) {
            rangeHashPartitionManager.create(rhr);
        }
    }

    /* detach 진행 */
    public void detachPartitionTable(PartitionTable partitionTable) {
        if (partitionTable.getRule() instanceof RangeHashRule rhr) {
            rangeHashPartitionManager.detach(rhr);
        }
    }

    public void dropPartitionTable(PartitionTable partitionTable) {
        if (partitionTable.getRule() instanceof RangeHashRule rhr) {
            rangeHashPartitionManager.drop(rhr.getDetachPartitionTableName());
        }
    }
}
