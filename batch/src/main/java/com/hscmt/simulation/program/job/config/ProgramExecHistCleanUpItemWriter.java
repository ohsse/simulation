package com.hscmt.simulation.program.job.config;

import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.program.dto.PgmExecHistDeleteDto;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ProgramExecHistCleanUpItemWriter implements ItemWriter<ProgramExecHistDto> {
    private final VirtualEnvironmentComponent vcomp;
    private final ProgramExecHistRepository repository;

    @Override
    public void write(Chunk<? extends ProgramExecHistDto> chunk) throws Exception {
        Set<String> deleteHistIds = new HashSet<>();
        chunk.getItems().forEach(item -> {
           String deleteFilePath = FileUtil.getDirPath(vcomp.getProgramBasePath(), item.getPgmId(), vcomp.getEXEC_RESULT_DIR(), item.getRsltDirId());
           FileUtil.retryDelete(deleteFilePath);
           deleteHistIds.add(item.getHistId());
        });


        if (!deleteHistIds.isEmpty()) {
            log.info("delete hist ids : {}", deleteHistIds);
            repository.deleteAllByIdInBatch(deleteHistIds);
        }
    }
}
