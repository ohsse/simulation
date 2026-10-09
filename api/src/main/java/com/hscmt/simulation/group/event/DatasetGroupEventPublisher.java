package com.hscmt.simulation.group.event;

import com.hscmt.simulation.group.domain.DatasetGroup;
import com.hscmt.simulation.group.repository.GroupBaseRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DatasetGroupEventPublisher extends GroupEventPublisher<DatasetGroup> {
    protected DatasetGroupEventPublisher(GroupBaseRepository<DatasetGroup> repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }
}
