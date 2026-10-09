package com.hscmt.simulation.program.event;

import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramResultEventPublisher extends AbstractDomainEventPublisher<ProgramResult> {
    public ProgramResultEventPublisher(ProgramResultRepository repository, ApplicationEventPublisher eventPublisher) {
        super(repository, eventPublisher);
    }

    public void deleteAllAndPublish (List<ProgramResult> deleteEntities) {
        for (ProgramResult item : deleteEntities) {
            super.publishAndClear(item, new ProgramResultDeletedEvent(item.getPgmId(), item.getRsltId(), item.getRsltNm(), item.getFileXtns()));
        }
        super.repository.deleteAllInBatch(deleteEntities);
    }

}
