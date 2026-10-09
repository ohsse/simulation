package com.hscmt.simulation.group.event;

import com.hscmt.simulation.group.domain.LayerGroup;
import com.hscmt.simulation.group.repository.GroupBaseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LayerGroupEventPublisher extends GroupEventPublisher<LayerGroup>{
    protected LayerGroupEventPublisher(GroupBaseRepository<LayerGroup> repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }
}
