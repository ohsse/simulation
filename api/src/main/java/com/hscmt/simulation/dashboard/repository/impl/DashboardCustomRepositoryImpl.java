package com.hscmt.simulation.dashboard.repository.impl;

import com.hscmt.common.enumeration.StructType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dashboard.domain.Dashboard;
import com.hscmt.simulation.dashboard.domain.QDashboard;
import com.hscmt.simulation.dashboard.dto.DashboardDto;
import com.hscmt.simulation.dashboard.repository.DashboardCustomRepository;
import com.hscmt.simulation.group.domain.QDashboardGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardCustomRepositoryImpl implements DashboardCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<DashboardDto> findAllDashboards(String grpId) {
        QDashboard qDashboard = QDashboard.dashboard;
        QDashboardGroup qDashboardGroup = QDashboardGroup.dashboardGroup;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qDashboard.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(DashboardDto.class, DashboardDto.projectionFields(qDashboard))
                )
                .from(qDashboard)
                .leftJoin(qDashboardGroup).on(qDashboard.grpId.eq(qDashboardGroup.grpId))
                .where(builder)
                .orderBy(qDashboardGroup.sortOrd.asc().nullsLast() ,qDashboard.sortOrd.asc().nullsLast())
                .fetch();
    }

    @Override
    public DashboardDto findDashboardDtoById(String dsId) {
        QDashboard qDashboard = QDashboard.dashboard;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(DashboardDto.class, DashboardDto.projectionFields(qDashboard))
                )
                .from(qDashboard)
                .where(qDashboard.dsbdId.eq(dsId))
                .fetchOne();
    }

    @Override
    public void updateGrpIdToNull(String grpId) {
        QDashboard qDashboard = QDashboard.dashboard;
        queryFactory.update(qDashboard)
                .set(qDashboard.grpId, (String) null)
                .where(qDashboard.grpId.eq(grpId))
                .execute();
    }

    @Override
    public void updateGrpIdToNull(List<String> grpIds) {
        QDashboard qDashboard = QDashboard.dashboard;
        queryFactory.update(qDashboard)
                .set(qDashboard.grpId, (String) null)
                .where(qDashboard.grpId.in(grpIds))
                .execute();
    }

    @Override
    public List<Dashboard> findAllDashboardByVisIds(List<String> visIds) {
        QDashboard qDashboard = QDashboard.dashboard;
        return queryFactory
                .selectFrom(qDashboard)
                .where(
                        Expressions.booleanTemplate(
                                """
                                        exists (
                                            select 1
                                            from jsonb_array_elements({0}) elem 
                                            where elem ->> 'structType' = {1} 
                                            and elem ->> 'structValue' = any({2}::text[])
                                        )
                                        """,
                                qDashboard.items,
                                Expressions.constant(StructType.DYNAMIC),
                                Expressions.constant(visIds)

                        )
                )
                .fetch();
    }
}
