package com.hscmt.simulation.collect.job.auto.step;

import com.hscmt.simulation.collect.dto.TagCollectDto;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@StepScope
@Component
public class TagAutoCollectItemReader implements ItemReader<TagCollectDto> {
    private Iterator<TagCollectDto> iterator;

    @Value("#{stepExecutionContext['targetList']}")
    private List<TagCollectDto> targetList;

    @Override
    public TagCollectDto read () {
        if (iterator == null ) {
            iterator = targetList.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
