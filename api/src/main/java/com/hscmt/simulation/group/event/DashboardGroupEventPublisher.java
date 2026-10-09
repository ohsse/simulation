package com.hscmt.simulation.group.event;

import com.hscmt.simulation.group.domain.DashboardGroup;
import com.hscmt.simulation.group.repository.GroupBaseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DashboardGroupEventPublisher extends GroupEventPublisher <DashboardGroup> {
    protected DashboardGroupEventPublisher(GroupBaseRepository<DashboardGroup> repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }
}
