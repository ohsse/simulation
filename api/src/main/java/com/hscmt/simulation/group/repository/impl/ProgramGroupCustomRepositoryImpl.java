package com.hscmt.simulation.group.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QProgramGroup;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.repository.ProgramGroupCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramGroupCustomRepositoryImpl implements ProgramGroupCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupDto> findAllProgramGroups(String grpId) {
        QProgramGroup qProgramGroup = QProgramGroup.programGroup;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qProgramGroup.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(GroupDto.class, GroupDto.projectionFields(qProgramGroup))
                )
                .from(qProgramGroup)
                .where(builder)
                .orderBy(qProgramGroup.sortOrd.asc())
                .fetch();
    }
}
