package com.hscmt.simulation.group.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QLayerGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.repository.LayerGroupCustomRepository;
import com.hscmt.simulation.layer.domain.QLayer;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LayerGroupCustomRepositoryImpl implements LayerGroupCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupDto> findAllLayerGroups(String grpId) {
        QLayerGroup qLayerGroup = QLayerGroup.layerGroup;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qLayerGroup.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(GroupDto.class, GroupDto.projectionFields(qLayerGroup))
                )
                .from(qLayerGroup)
                .where(builder)
                .orderBy(qLayerGroup.sortOrd.asc())
                .fetch();
    }
}
