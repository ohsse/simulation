package com.hscmt.simulation.layer.repository;

import com.hscmt.simulation.layer.domain.Layer;
import com.hscmt.simulation.layer.domain.QLayer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LayerRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    public Layer findLayer (String layerId) {
        return queryFactory.selectFrom(QLayer.layer)
                .where(QLayer.layer.layerId.eq(layerId))
                .fetchOne();
    }
}
