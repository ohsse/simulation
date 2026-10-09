package com.hscmt.simulation.group.event;

import com.hscmt.common.enumeration.GroupType;
import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.group.domain.*;
import com.hscmt.simulation.group.repository.GroupBaseRepository;
import org.springframework.context.ApplicationEventPublisher;

public abstract class GroupEventPublisher <E extends GroupBase> extends AbstractDomainEventPublisher<E> {

    protected GroupEventPublisher (GroupBaseRepository<E> repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }

    /* 그룹 삭제 후 이벤트 발행 */
    public void deleteAndPublish (E entity) {
        GroupType groupType = null;
        if (entity instanceof DatasetGroup) {
            groupType = GroupType.DATASET;
        } else if (entity instanceof DashboardGroup) {
            groupType = GroupType.DASHBOARD;
        } else if (entity instanceof ProgramGroup) {
            groupType = GroupType.PROGRAM;
        } else if (entity instanceof LayerGroup) {
            groupType = GroupType.LAYER;
        }

        super.deleteAndPublish(entity, new GroupDeletedEvent(entity.getGrpId(), groupType));
    }

}
