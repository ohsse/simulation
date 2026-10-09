package com.hscmt.simulation.partition.job.config.partition;

import com.hscmt.simulation.partition.rule.PartitionRule;
import com.hscmt.simulation.partition.rule.RangeHashRule;
import com.hscmt.simulation.partition.spec.PartitionTable;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@StepScope
@RequiredArgsConstructor
@Component
public class ProgramExecHistPartitioner implements Partitioner {
    @Qualifier("simulationJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        PartitionTable histTable = PartitionTable.PGM_EXEC_H;
        PartitionRule rule = histTable.getRule();

        assert(rule instanceof RangeHashRule);

        List<String> subPartitions = jdbcTemplate.query(
                "SELECT c.relname AS child_table " +
                        "FROM pg_inherits i " +
                        "JOIN pg_class c ON i.inhrelid = c.oid " +
                        "JOIN pg_class p ON i.inhparent = p.oid " +
                        "WHERE p.relname = ?",
                (rs, i) -> rs.getString("child_table"),
                ((RangeHashRule) rule).getDetachPartitionTableName()
        );
        Map<String, ExecutionContext> result = new LinkedHashMap<>();
        int idx = 0;
        for (String sub : subPartitions) {
            ExecutionContext ctx = new ExecutionContext();
            ctx.putString("subPartitionTableName", sub);   // 예: hist_202501_p3
            result.put("partition_" + (idx++), ctx);
        }
        return result;
    }
}
