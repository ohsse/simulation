package com.hscmt.simulation.venv.event;

import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.venv.domain.VirtualEnvironment;
import com.hscmt.simulation.venv.dto.VenvUpdateDto;
import com.hscmt.simulation.venv.repository.VenvRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VenvEventPublisher extends AbstractDomainEventPublisher<VirtualEnvironment> {
    public VenvEventPublisher(VenvRepository repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }

    /* 저장 후 이벤트 발행 */
    public VirtualEnvironment saveAndPublish (VirtualEnvironment newEntity, List<String> lbrIds) {
        return super.saveAndPublish(
                newEntity,
                saveEntity -> new VenvCreatedEvent(saveEntity.getVenvId(), saveEntity.getPyVrsn(), lbrIds)
        );
    }

    /* 수정 후 이벤트 발행 */
    public void updateAndPublish (VirtualEnvironment entity, VenvUpdateDto dto) {
        entity.update(dto);
        super.publishAndClear(entity, new VenvUpdatedEvent(entity.getVenvId(), dto.getAddLbrIds(), dto.getDelLbrNms()));
    }

    /* 삭제 후 이벤트 발행 */
    public void deleteAndPublish (VirtualEnvironment entity) {
        super.deleteAndPublish(entity, new VenvDeletedEvent(entity.getVenvId()));
    }
}
