package com.hscmt.simulation.program.event;

import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.program.domain.ProgramExecHist;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProgramExecHistEventPublisher extends AbstractDomainEventPublisher<ProgramExecHist> {

    public ProgramExecHistEventPublisher(ProgramExecHistRepository repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }

    public void deleteAndPublish(ProgramExecHist entity) {
        super.deleteAndPublish(entity, new ProgramExecDeletedEvent(entity.getPgmId(), entity.getRsltDirId()));
    }

    public void successAndPublish (ProgramExecHist entity) {
        entity.success();
        repository.saveAndFlush(entity);
        super.publishAndClear(entity,new ProgramExecSuccessEvent(entity.getPgmId(), entity.getHistId()));
    }

    public void deleteAllAndPublish (List<ProgramExecHist> entityList) {
        for (ProgramExecHist entity : entityList) {
            super.publishAndClear(entity, new ProgramExecDeletedEvent(entity.getPgmId(), entity.getRsltDirId()));
        }
        repository.deleteAllInBatch(entityList);
    }
}
