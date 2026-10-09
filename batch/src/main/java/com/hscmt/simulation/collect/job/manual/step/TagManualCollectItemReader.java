package com.hscmt.simulation.collect.job.manual.step;

import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.waternet.tag.dto.TagDataDto;
import com.hscmt.waternet.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
@StepScope
public class TagManualCollectItemReader implements ItemStreamReader<TagDataDto> {

    @Value("#{stepExecutionContext['targetList']}")
    private List<TagCollectDto> targetList;

    private int targetIdx = -1;
    private Iterator<TagDataDto> currentIter;

    private final TagService tagService;

    @Override
    public TagDataDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        while (true) {
            if (currentIter != null && currentIter.hasNext()) {
                return currentIter.next();
            }

            targetIdx++;
            if (targetList == null || targetIdx >= targetList.size()) {
                return null;
            }

            TagCollectDto dto = currentTarget();
            List<TagDataDto> rows = tagService.findTagDataList(dto);
            currentIter = rows.iterator();
        }
    }

    private TagCollectDto currentTarget() {
        return targetList.get(targetIdx);
    }



    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        targetIdx = -1;
        currentIter = null;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        ItemStreamReader.super.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        ItemStreamReader.super.close();
    }

}
