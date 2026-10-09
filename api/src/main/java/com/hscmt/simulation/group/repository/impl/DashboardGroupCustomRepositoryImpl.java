package com.hscmt.simulation.group.repository.impl;


import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QDashboardGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.repository.DashboardGroupCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class DashboardGroupCustomRepositoryImpl implements DashboardGroupCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupDto> findAllDashboardGroups(String grpId) {
        QDashboardGroup qDashboardGroup = QDashboardGroup.dashboardGroup;

        BooleanBuilder builder = new BooleanBuilder();

        if (grpId != null) {
            builder.and(qDashboardGroup.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(GroupDto.class, GroupDto.projectionFields(qDashboardGroup))
                )
                .from(qDashboardGroup)
                .where(builder)
                .orderBy(qDashboardGroup.sortOrd.asc())
                .fetch();
    }
}
