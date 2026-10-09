package com.hscmt.simulation.layer.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QLayerGroup;
import com.hscmt.simulation.layer.domain.QLayer;
import com.hscmt.simulation.layer.dto.LayerDto;
import com.hscmt.simulation.layer.repository.LayerCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LayerCustomRepositoryImpl implements LayerCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<LayerDto> findAllLayers (String grpId) {
        QLayer qLayer = QLayer.layer;
        QLayerGroup qLayerGroup = QLayerGroup.layerGroup;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qLayer.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(LayerDto.class, LayerDto.projectionFields(qLayer))
                )
                .from(qLayer)
                .leftJoin(qLayerGroup).on(qLayer.grpId.eq(qLayerGroup.grpId))
                .where(builder)
                .orderBy(qLayerGroup.sortOrd.asc().nullsLast() ,qLayer.sortOrd.asc().nullsLast())
                .fetch();
    }

    @Override
    public LayerDto findLayerDtoById(String layerId) {
        QLayer qLayer = QLayer.layer;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(LayerDto.class, LayerDto.projectionFields(qLayer))
                )
                .from(qLayer)
                .where(qLayer.layerId.eq(layerId))
                .fetchOne();
    }

    @Override
    public void updateGrpIdToNull(String grpId) {
        QLayer qLayer = QLayer.layer;
        queryFactory.update(qLayer)
                .set(qLayer.grpId, (String) null)
                .where(qLayer.layerId.eq(grpId))
                .execute();
    }

    @Override
    public void updateGrpIdToNull(List<String> grpIds) {
        QLayer qLayer = QLayer.layer;
        queryFactory.update(qLayer)
                .set(qLayer.grpId, (String) null)
                .where(qLayer.layerId.in(grpIds))
                .execute();
    }


}
