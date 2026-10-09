package com.hscmt.simulation.program.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.group.domain.QProgramGroup;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramVisualization;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.hscmt.simulation.program.repository.ProgramCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ProgramCustomRepositoryImpl implements ProgramCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public void updateProgramVenvToNull(String venvId) {
        QProgram qProgram = QProgram.program;
        queryFactory
                .update(qProgram)
                .set(qProgram.venvId, (String) null)
                .where(qProgram.venvId.eq(venvId))
                .execute();
    }

    @Override
    public List<ProgramDto> findAllPrograms(String grpId) {
        QProgram qProgram = QProgram.program;
        QProgramGroup qProgramGroup = QProgramGroup.programGroup;
        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(qProgram.grpId.eq(grpId));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramDto.class, ProgramDto.projectionFields(qProgram))
                )
                .from(qProgram)
                .leftJoin(qProgramGroup).on(qProgram.grpId.eq(qProgramGroup.grpId))
                .where(builder)
                .orderBy(qProgramGroup.sortOrd.asc().nullsLast(),qProgram.sortOrd.asc().nullsLast(), qProgram.rltmYn.desc())
                .fetch();
    }

    @Override
    public ProgramDto findProgramById(String pgmId) {
        QProgram qProgram = QProgram.program;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramDto.class, ProgramDto.projectionFields(qProgram))
                )
                .from(qProgram)
                .where(qProgram.pgmId.eq(pgmId))
                .fetchOne();
    }

    @Override
    public void updateGrpIdToNull(String grpId) {
        QProgram qProgram = QProgram.program;
        queryFactory.update(qProgram)
                .set(qProgram.grpId, (String) null)
                .where(qProgram.grpId.eq(grpId))
                .execute();
    }

    @Override
    public void updateGrpIdToNull(List<String> grpIds) {
        QProgram qProgram = QProgram.program;
        queryFactory.update(qProgram)
                .set(qProgram.grpId, (String) null)
                .where(qProgram.grpId.in(grpIds))
                .execute();
    }

    @Override
    public ProgramDto findProgramByVisId(String visId) {
        QProgram qProgram = QProgram.program;
        QProgramVisualization qProgramVisualization = QProgramVisualization.programVisualization;
        return queryFactory
                .select(QProjectionUtil.toQBean(ProgramDto.class, ProgramDto.projectionFields(qProgram)))
                .from(qProgram)
                .where(
                        JPAExpressions
                                .selectOne()
                                .from(qProgramVisualization)
                                .where(
                                        qProgram.pgmId.eq(qProgramVisualization.pgmId)
                                                .and(qProgramVisualization.visId.eq(visId))
                                )
                                .exists()
                )
                .fetchOne();
    }
}
