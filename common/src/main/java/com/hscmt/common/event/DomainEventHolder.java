package com.hscmt.common.event;

import java.util.List;

public interface DomainEventHolder {
    void registerEvent(Object event);
    List<Object> getDomainEvents();
    void clearDomainEvents();
}
