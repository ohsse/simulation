package com.hscmt.simulation.program.repository.impl;


import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.ProgramVisualization;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramVisualization;
import com.hscmt.simulation.program.dto.ProgramVisualizationDto;
import com.hscmt.simulation.program.repository.ProgramVisualizationCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramVisualizationCustomRepositoryImpl implements ProgramVisualizationCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllByPgmId(String pgmId) {
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        queryFactory
                .delete(qProgramVisualization)
                .where(setBorderCondition(qProgramVisualization, pgmId))
                .execute();
    }

    @Override
    public List<ProgramVisualization> findAllByPgmId (String pgmId) {
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        return queryFactory
                .selectFrom(qProgramVisualization)
                .where(setBorderCondition(qProgramVisualization, pgmId))
                .fetch();
    }

    @Override
    public List<ProgramVisualizationDto> findAllProgramVisualizations(String pgmId) {
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramVisualizationDto.class, ProgramVisualizationDto.projectionFields(qProgramVisualization)))
                .from(qProgramVisualization)
                .where(setBorderCondition(qProgramVisualization, pgmId))
                .orderBy(qProgramVisualization.rgstDttm.asc())
                .fetch();
    }

    @Override
    public List<ProgramVisualizationDto> findAllProgramVisualizationsByGrpId(String grpId) {
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        QProgram qProgram = QProgram.program;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(
                    JPAExpressions.selectOne()
                            .from(qProgram)
                            .where(
                                    qProgramVisualization.pgmId.eq(qProgram.pgmId)
                                            .and(qProgram.grpId.eq(grpId))
                            )
                            .exists()
            );
        }

        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramVisualizationDto.class, ProgramVisualizationDto.projectionFields(qProgramVisualization)))
                .from(qProgramVisualization)
                .where(builder)
                .orderBy(qProgramVisualization.rgstDttm.asc())
                .fetch();
    }

    private BooleanBuilder setBorderCondition(QProgramVisualization qProgramVisualization, String pgmId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (pgmId != null) {
            builder.and(qProgramVisualization.pgmId.eq(pgmId));
        }
        return builder;
    }

    public ProgramVisualizationDto findByVisId (String visId) {
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramVisualizationDto.class, ProgramVisualizationDto.projectionFields(qProgramVisualization)))
                .from(qProgramVisualization)
                .where(qProgramVisualization.visId.eq(visId))
                .fetchOne();
    }
}
