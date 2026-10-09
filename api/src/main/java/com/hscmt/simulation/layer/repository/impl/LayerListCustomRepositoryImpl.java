package com.hscmt.simulation.layer.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.layer.domain.QLayer;
import com.hscmt.simulation.layer.domain.QLayerList;
import com.hscmt.simulation.layer.dto.LayerFeatureDto;
import com.hscmt.simulation.layer.repository.LayerListCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LayerListCustomRepositoryImpl implements LayerListCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPQLQueryFactory queryFactory;

    @Override
    public void deleteAllByLayerId(String layerId) {
        QLayerList qLayerList = QLayerList.layerList;
        queryFactory
                .delete(qLayerList)
                .where(qLayerList.id.layerId.eq(layerId))
                .execute();
    }

    @Override
    public List<LayerFeatureDto> findLayerFeatures(String layerId, Double minX, Double minY, Double maxX, Double maxY) {
        QLayerList qLayerList = QLayerList.layerList;
        QLayer qLayer = QLayer.layer;

        BooleanBuilder builder = new BooleanBuilder();

        if (minX != null && maxX != null && minY != null && maxY != null) {
            builder.and(Expressions.booleanTemplate(
                    "{0} && ST_MakeEnvelope({1}, {2}, {3}, {4}, 5186)",
                    qLayerList.gmtrVal, minX, minY, maxX, maxY
            ));
        }

        if (layerId != null && !layerId.isEmpty()) builder.and(qLayer.layerId.eq(layerId));

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(LayerFeatureDto.class, LayerFeatureDto.projectionFields(qLayerList, qLayer))
                )
                .from(qLayer)
                .join(qLayerList).on(qLayer.layerId.eq(qLayerList.id.layerId)).fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public LayerFeatureDto findLayerFeatureInfo(String layerId, Long fid) {
        QLayerList qLayerList = QLayerList.layerList;
        QLayer qLayer = QLayer.layer;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(LayerFeatureDto.class, LayerFeatureDto.projectionFields(qLayerList, qLayer))
                )
                .from(qLayer)
                .join(qLayerList).on(qLayer.layerId.eq(qLayerList.id.layerId))
                .where(
                        qLayer.layerId.eq(layerId)
                                .and(qLayerList.id.fid.eq(fid))
                )
                .fetchOne();
    }
}
