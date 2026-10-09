package com.hscmt.simulation.group.event;

import com.hscmt.simulation.group.domain.ProgramGroup;
import com.hscmt.simulation.group.repository.GroupBaseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProgramGroupEventPublisher  extends GroupEventPublisher<ProgramGroup> {
    protected ProgramGroupEventPublisher(GroupBaseRepository<ProgramGroup> repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }
}
