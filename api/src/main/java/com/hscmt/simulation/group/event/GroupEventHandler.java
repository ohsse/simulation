package com.hscmt.simulation.group.event;

import com.hscmt.simulation.group.service.GroupServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GroupEventHandler {

    private final GroupServiceFactory groupServiceFactory;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEvent (GroupDeletedEvent event) {
        groupServiceFactory.getService(event.groupType()).updateGroupIdToNull(event.grpId());
    }
}
