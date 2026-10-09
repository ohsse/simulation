package com.hscmt.simulation.program.event;

import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.program.domain.ProgramInputFile;
import com.hscmt.simulation.program.repository.ProgramInputFileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramInputFileEventPublisher extends AbstractDomainEventPublisher<ProgramInputFile> {
    public ProgramInputFileEventPublisher(ProgramInputFileRepository repository, ApplicationEventPublisher eventPublisher) {
        super(repository, eventPublisher);
    }

    public void deleteAllAndPublish(List<ProgramInputFile> deleteEntities, List<String> fileNames) {
        for (ProgramInputFile entity : deleteEntities) {
            super.publishAndClear(entity, new ProgramInputFileDeletedEvent(entity.getPgmId(), fileNames));
        }
        super.repository.deleteAllInBatch(deleteEntities);
    }

    public void publishAndClear(ProgramInputFile entity, List<String> fileNames) {
        super.publishAndClear(entity, new ProgramInputFileDeletedEvent(entity.getPgmId(), fileNames));
    }
}
