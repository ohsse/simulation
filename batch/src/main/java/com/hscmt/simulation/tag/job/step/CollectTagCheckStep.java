package com.hscmt.simulation.tag.job.step;

import com.hscmt.simulation.tag.mapper.WaternetTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@StepScope
@RequiredArgsConstructor
@Slf4j
@Component
public class CollectTagCheckStep implements Tasklet {

    private final WaternetTagMapper mapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        /* 수집이 더는 필요하지 않은 태그 정리 */
        mapper.updateNoneCollectableTag();
        /* 수집 필요한 태그 세팅 */
        mapper.updateCollectableTag();
        return RepeatStatus.FINISHED;
    }
}
