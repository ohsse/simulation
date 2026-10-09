package com.hscmt.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Function;

public abstract class AbstractDomainEventPublisher <T extends DomainEventHolder> {

    protected final JpaRepository<T, ?> repository;
    protected final ApplicationEventPublisher publisher;

    protected AbstractDomainEventPublisher(JpaRepository<T, ?> repository,
                                           ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    protected T saveAndPublish(T entity, Function<T, Object> eventFunction) {
        T saveEntity = repository.save(entity);

        if (eventFunction != null) {
            Object event = eventFunction.apply(saveEntity);
            saveEntity.registerEvent(event);
        }

        this.publishAndClear(saveEntity);

        return saveEntity;
    }

    protected void deleteAndPublish(T entity, Object event) {
        entity.registerEvent(event);
        repository.delete(entity);
        this.publishAndClear(entity);
    }

    protected void publishAndClear(T entity) {
        entity.getDomainEvents().forEach(publisher::publishEvent);
        entity.clearDomainEvents();
    }

    protected void publishAndClear (T entity, Object event) {
        entity.registerEvent(event);
        this.publishAndClear(entity);
    }

}
