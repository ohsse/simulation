package com.hscmt.common.domain;

import com.hscmt.common.event.DomainEventHolder;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MappedSuperclass
public abstract class DomainEventEntity extends BaseEntity implements DomainEventHolder {

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    @Override
    public void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    @Override
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
