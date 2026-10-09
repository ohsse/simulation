package com.hscmt.simulation.partition.job.config.partition;

import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@StepScope
@Component
@RequiredArgsConstructor
public class ProgramExecHistItemWriter implements ItemWriter<ProgramExecHistDto> {

    private final VirtualEnvironmentComponent vcomp;

    @Override
    public void write(Chunk<? extends ProgramExecHistDto> chunk) throws Exception {
        Set<String> paths = chunk.getItems().stream()
                .map(dto -> FileUtil.getDirPath(
                        vcomp.getProgramBasePath(),
                        dto.getPgmId(),
                        vcomp.getEXEC_RESULT_DIR(),
                        dto.getRsltDirId()
                ))
                .collect(Collectors.toSet());
        for (String path : paths) {
            FileUtil.retryDelete(path);
        }
    }
}
