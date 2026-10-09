package com.hscmt.simulation.collect.job.step;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.simulation.collect.mapper.CollectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
@Slf4j
public class TagCollectItemWriter implements ItemWriter<MsrmUpsertDto> {
    private final CollectMapper mapper;

    @Override
    public void write(Chunk<? extends MsrmUpsertDto> chunk) throws Exception {
        log.info("TagCollectItemWriter write chunk size: {}", chunk.size());
        chunk.forEach(item -> {
            if (item != null) {
                mapper.upsertTagData(item);
            }
        });
    }
}
