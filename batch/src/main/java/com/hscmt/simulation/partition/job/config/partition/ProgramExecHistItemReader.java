package com.hscmt.simulation.partition.job.config.partition;

import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@StepScope
@Component
@RequiredArgsConstructor
public class ProgramExecHistItemReader implements ItemStreamReader<ProgramExecHistDto> {

    @Value("#{stepExecutionContext['subPartitionTableName']}")
    private String subPartitionTableName;

    @Qualifier("simulationDataSource")
    private final DataSource dataSource;

    private JdbcCursorItemReader<ProgramExecHistDto> delegate;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        String sql = String.format(
                "SELECT pgm_id, rslt_dir_id FROM %s where exec_stts_cd = 'COMPLETED'",
                subPartitionTableName  // ✅ 이제 값이 주입된 후에 사용
        );

        this.delegate = new JdbcCursorItemReaderBuilder<ProgramExecHistDto>()
                .name("programExecHistItemReader_" + subPartitionTableName)
                .dataSource(dataSource)
                .fetchSize(1000)
                .sql(sql)
                .rowMapper((rs, i) -> {
                    ProgramExecHistDto dto = new ProgramExecHistDto();
                    dto.setPgmId(rs.getString("pgm_id"));
                    dto.setRsltDirId(rs.getString("rslt_dir_id"));
                    return dto;
                })
                .build();

        delegate.open(executionContext);
    }

    @Override
    public ProgramExecHistDto read() throws Exception {
        return delegate.read();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }
}
