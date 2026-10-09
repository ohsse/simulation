package com.hscmt.simulation.partition.service;

import com.hscmt.common.util.PartitionCheckUtil;
import com.hscmt.simulation.partition.mapper.PartitionMapper;
import com.hscmt.simulation.partition.rule.RangeHashRule;
import com.hscmt.simulation.partition.rule.type.HashPartition;
import com.hscmt.simulation.partition.spec.type.PartitionRangeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RangeHashPartitionManager {

    private final PartitionMapper mapper;

    public void drop (String partitionTableName) {
        mapper.dropTable(partitionTableName);
    }

    public void detach (RangeHashRule rule) {
        /* 파티션테이블 detach */
        mapper.detachPartitionTable(rule.getTableName(), rule.getDetachPartitionTableName());
    }

    public void create (RangeHashRule rule) {
        /* 파티션 원본 테이블 */
        String tableName = rule.getTableName();

        /* 특정 테이블의 마지막 파티션 테이블명 가져오기 */
        String lastPartitionTableName = mapper.getLastPartitionTableName(tableName);

        /* 현재시점에서 필요한 모든 파티션 테이블명 가져오기 */
        List<String> needCreatePartitionTableNames = PartitionCheckUtil.getAllNeededPartitionNames(rule, lastPartitionTableName);

        for (String partitionTableName : needCreatePartitionTableNames) {
            /* 파티션 레인지 정보 가져오기 */
            Map<String, Object> rangePartitionInfoMap = PartitionCheckUtil.getRangePartitionInfoMap(rule, partitionTableName);
            log.info("create partitionTableName : {}", partitionTableName);
            /* 파티션 테이블 생성 */
            mapper.createRangePartitionTable(rangePartitionInfoMap);

            /* Range + Hash Composite Partition인 경우 */
            if (rule instanceof HashPartition hashPartition) {
                /* 추가한 Range Partition table에 hash Subpartition 추가 */
                Integer moduleSize = hashPartition.getHashCount();

                for (int i = 0; i < moduleSize; i ++) {
                    String subPartitionTableName = partitionTableName + "_" + i;
                    log.info("create subPartitionTableName : {}", subPartitionTableName);
                    /* 서브파티션 테이블 추가 */
                    mapper.createSubHashPartitionTable(partitionTableName,subPartitionTableName,moduleSize,i);
                }
            }
        }
    }


}
