package com.hscmt.simulation.program.event;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.program.domain.Program;
import com.hscmt.simulation.program.dto.ProgramUpsertDto;
import com.hscmt.simulation.program.repository.ProgramRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProgramEventPublisher extends AbstractDomainEventPublisher<Program> {
    public ProgramEventPublisher(ProgramRepository repository, ApplicationEventPublisher eventPublisher) {
        super(repository, eventPublisher);
    }

    /* 프로그램 삭제하고 삭제 이벤트 발행 */
    public void deleteAndPublish(Program entity) {
        super.deleteAndPublish(entity, new ProgramDeletedEvent(entity.getPgmId()));
    }

    /* 프로그램 정보 수정 후 이벤트 발행 */
    public void updateAndPublish (Program entity, ProgramUpsertDto dto) {
        entity.changeInfo(dto);
        super.publishAndClear(entity
                , getUpsertEvent(entity));
    }

    /* 신규 프로그램 저장 후 이벤트 발행 */
    public Program saveAndPublish (Program newEntity) {
        return super.saveAndPublish(
                newEntity,
                saveEntity -> getUpsertEvent(saveEntity)
        );
    }

    private ProgramUpsertedEvent getUpsertEvent (Program entity) {
        return new ProgramUpsertedEvent(
                entity.getPgmId(),
                entity.getStrtExecDttm(),
                entity.getRpttIntvTypeCd(),
                entity.getRpttIntvVal(),
                entity.getRltmYn()
        );
    }


}
