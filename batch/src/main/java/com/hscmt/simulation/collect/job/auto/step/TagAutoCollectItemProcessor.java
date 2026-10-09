package com.hscmt.simulation.collect.job.auto.step;

import com.hscmt.simulation.collect.dto.MsrmUpsertDto;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.waternet.tag.dto.TagDataDto;
import com.hscmt.waternet.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class TagAutoCollectItemProcessor implements ItemProcessor<TagCollectDto, MsrmUpsertDto> {

    private final TagService tagService;
    @Value("#{jobParameters['jobExecutor']}")
    private String jobExecutor;

    @Override
    public MsrmUpsertDto process (TagCollectDto item) {
        TagDataDto data = tagService.findTagData(item);
        if (data == null) return null;
        return new MsrmUpsertDto(data, jobExecutor);
    }
}
