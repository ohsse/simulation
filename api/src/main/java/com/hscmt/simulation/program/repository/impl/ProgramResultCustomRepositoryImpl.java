package com.hscmt.simulation.program.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramResult;
import com.hscmt.simulation.program.dto.ProgramResultDto;
import com.hscmt.simulation.program.repository.ProgramResultCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramResultCustomRepositoryImpl implements ProgramResultCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;


    @Override
    public List<ProgramResult> findAllByPgmId(String pgmId) {
        QProgramResult qProgramResult = QProgramResult.programResult;
        return queryFactory
                .selectFrom(qProgramResult)
                .where(qProgramResult.pgmId.eq(pgmId))
                .fetch();
    }

    @Override
    public List<ProgramResultDto> findAllProgramResults(String pgmId) {
        QProgramResult qProgramResult = QProgramResult.programResult;

        BooleanBuilder builder = new BooleanBuilder();

        if (pgmId != null && !pgmId.isBlank()) {
            builder.and(qProgramResult.pgmId.eq(pgmId));
        }

        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramResultDto.class, ProgramResultDto.projectionFields(qProgramResult)))
                .from(qProgramResult)
                .where(builder)
                .fetch();
    }

    @Override
    public List<ProgramResultDto> findAllProgramResultsByGrpId(String grpId) {
        QProgramResult qProgramResult = QProgramResult.programResult;
        QProgram qProgram = QProgram.program;
        BooleanBuilder builder = new BooleanBuilder();

        if (grpId != null && !grpId.isBlank()) {
            builder.and(
                    JPAExpressions
                    .selectOne()
                    .from(qProgram)
                            .where(qProgram.pgmId.eq(qProgramResult.pgmId) .and(qProgram.grpId.eq(grpId)))
                            .exists()
            );
        }
        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramResultDto.class, ProgramResultDto.projectionFields(qProgramResult)))
                .from(qProgramResult)
                .where(builder)
                .fetch();
    }

    @Override
    public void deleteAllByPgmId(String pgmId) {
        QProgramResult qProgramResult = QProgramResult.programResult;
        queryFactory
                .delete(qProgramResult)
                .where(qProgramResult.pgmId.eq(pgmId))
                .execute();
    }
}
