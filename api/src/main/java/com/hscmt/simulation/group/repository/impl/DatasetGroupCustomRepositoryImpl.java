package com.hscmt.simulation.group.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QDatasetGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.repository.DatasetGroupCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DatasetGroupCustomRepositoryImpl implements DatasetGroupCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupDto> findAllDatasetGroups(String grpId) {
        QDatasetGroup qDatasetGroup = QDatasetGroup.datasetGroup;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qDatasetGroup.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(GroupDto.class, GroupDto.projectionFields(qDatasetGroup))
                )
                .from(qDatasetGroup)
                .where(builder)
                .orderBy(qDatasetGroup.sortOrd.asc())
                .fetch();
    }

}
