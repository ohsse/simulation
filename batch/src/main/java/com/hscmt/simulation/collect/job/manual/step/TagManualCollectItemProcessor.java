package com.hscmt.simulation.collect.job.manual.step;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.waternet.tag.dto.TagDataDto;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
public class TagManualCollectItemProcessor implements ItemProcessor<TagDataDto, MsrmUpsertDto> {
    @Value("#{jobParameters['jobExecutor']}")
    private String jobExecutor;

    @Override
    public MsrmUpsertDto process(TagDataDto item) throws Exception {
        if (item == null) return null;
        return new MsrmUpsertDto(item, jobExecutor);
    }
}
