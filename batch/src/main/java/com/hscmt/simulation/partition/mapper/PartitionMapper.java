package com.hscmt.simulation.partition.mapper;

import com.hscmt.simulation.common.config.mybatis.SimulationMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@SimulationMapper
public interface PartitionMapper {
    /* 특정 파티션 이전의 모든 파티션명 조회 */
    List<String> getPrevPartitionTableNames(@Param("tableName") String tableName, @Param("partitionTableName") String partitionTableName);

    /* 마지막 파티션 테이블 명 조회 */
    String getLastPartitionTableName(@Param("tableName") String tableName);

    /* 레인지 파티션 테이블 추가 */
    void createRangePartitionTable(Map<String, Object> partitionTableInfo);

    /* 해쉬 서브 파티션 생성 */
    void createSubHashPartitionTable(@Param("partitionTableName") String partitionTableName, @Param("subPartitionTableName") String subPartitionTableName, @Param("moduleSize") int moduleSize, @Param("moduleIndex") int moduleIndex);

    /* 파티션 테이블 분리하기 */
    void detachPartitionTable(@Param("tableName") String tableName, @Param("partitionTableName") String partitionTableName);

    void dropTable( @Param("tableName") String tableName);
}
