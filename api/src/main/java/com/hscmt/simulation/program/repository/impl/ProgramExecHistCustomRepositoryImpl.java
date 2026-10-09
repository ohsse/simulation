package com.hscmt.simulation.program.repository.impl;

import com.hscmt.common.enumeration.DirectionType;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramExecHist;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.hscmt.simulation.program.dto.ProgramExecSearchDto;
import com.hscmt.simulation.program.repository.ProgramExecHistCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramExecHistCustomRepositoryImpl implements ProgramExecHistCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllByPgmId(String pgmId) {
        QProgramExecHist qProgramExecHist = QProgramExecHist.programExecHist;
        queryFactory
                .delete(qProgramExecHist)
                .where(qProgramExecHist.pgmId.eq(pgmId))
                .execute();
    }

    @Override
    public List<ProgramExecHistDto> findAllProgramExecHistList(ProgramExecSearchDto searchDto) {
        QProgram qProgram = QProgram.program;
        QProgramExecHist qProgramExecHist = QProgramExecHist.programExecHist;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(qProgramExecHist, qProgram))
                )
                .from(qProgramExecHist)
                .join(qProgram).on(qProgramExecHist.pgmId.eq(qProgram.pgmId)).fetchJoin()
                .where(getConditionByDto(qProgram, qProgramExecHist, searchDto))
                .orderBy(qProgramExecHist.execStrtDttm.desc())
                .fetch();
    }

    @Override
    public ProgramExecHistDto findByHistId(String histId) {
        QProgramExecHist qProgramExecHist = QProgramExecHist.programExecHist;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(qProgramExecHist))
                )
                .from(qProgramExecHist)
                .where(qProgramExecHist.histId.eq(histId))
                .fetchOne();
    }

    @Override
    public ProgramExecHistDto findByPgmIdAndRsltDirId(String pgmId, String rsltDirId) {
        QProgramExecHist q = QProgramExecHist.programExecHist;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(q))
                )
                .from (q)
                .where(q.pgmId.eq(pgmId).and(q.rsltDirId.eq(rsltDirId)))
                .fetchOne();
    }

    @Override
    public ProgramExecHistDto findByPgmIdAndRsltDirIdUsingDirection(String pgmId, String rsltDirId, DirectionType direction) {
        QProgramExecHist q = QProgramExecHist.programExecHist;
        JPQLQuery<ProgramExecHistDto> query = queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(q))
                )
                .from(q)
                .where(getConditionUsingDirection(pgmId, rsltDirId, direction))
                .orderBy(direction == DirectionType.NEXT ? q.rsltDirId.asc() : q.rsltDirId.desc())
                .limit(1);

        return query.fetchOne();
    }

    @Override
    public List<ProgramExecHistDto> findByPgmIdAndRsltDirIdUsingDirection(String pgmId, String rsltDirId, DirectionType direction, Integer limit) {
        QProgramExecHist q = QProgramExecHist.programExecHist;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(q))
                )
                .from(q)
                .where(getConditionUsingDirection(pgmId, rsltDirId, direction))
                .orderBy(direction == DirectionType.NEXT ? q.rsltDirId.asc() : q.rsltDirId.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<ProgramExecHistDto> findByPgmIdAndExecStrtDttm(String pgmId, LocalDateTime startDateDttm, LocalDateTime endDateDttm) {
        QProgramExecHist q = QProgramExecHist.programExecHist;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramExecHistDto.class, ProgramExecHistDto.projectionFields(q))
                )
                .from(q)
                .where(
                        q.pgmId.eq(pgmId)
                                .and(q.execSttsCd.eq(ExecStat.COMPLETED))
                                .and(q.execStrtDttm.goe(startDateDttm))
                                .and(q.execStrtDttm.loe(endDateDttm))
                )
                .orderBy(q.execStrtDttm.asc())
                .fetch();
    }

    private BooleanBuilder getConditionUsingDirection (String pgmId, String rsltDirId, DirectionType direction) {
        BooleanBuilder builder = new BooleanBuilder();
        QProgramExecHist qProgramExecHist = QProgramExecHist.programExecHist;
        builder.and(qProgramExecHist.pgmId.eq(pgmId));

        if (direction == DirectionType.NEXT) {
            builder.and(qProgramExecHist.rsltDirId.gt(rsltDirId));
        } else {
            builder.and(qProgramExecHist.rsltDirId.lt(rsltDirId));
        }

        builder.and(qProgramExecHist.execSttsCd.eq(ExecStat.COMPLETED));

        return builder;
    }

    private BooleanBuilder getConditionByDto (QProgram qProgram, QProgramExecHist qProgramExecHist, ProgramExecSearchDto searchDto) {

        BooleanBuilder builder = new BooleanBuilder();

        /* 조회시작일시 */
        if (searchDto.getSearchStrtDttm() != null) {
            builder.and(qProgramExecHist.execStrtDttm.goe(searchDto.getSearchStrtDttm()));
        }

        /*조회종료일시*/
        if (searchDto.getSearchEndDttm() != null) {
            builder.and(qProgramExecHist.execEndDttm.loe(searchDto.getSearchEndDttm()));
        }

        /* 실시간프로그램여부 */
        if (searchDto.getRltmYn() != null) {
            builder.and(qProgram.rltmYn.eq(searchDto.getRltmYn()));
        }

        /* 프로그램 실행 상태 */
        if (searchDto.getExecSttsCd() != null) {
            builder.and(qProgramExecHist.execSttsCd.eq(searchDto.getExecSttsCd()));
        }

        /* 프로그램 실행 유형 */
        if (searchDto.getExecTypeCd() != null) {
            builder.and(qProgramExecHist.execTypeCd.eq(searchDto.getExecTypeCd()));
        }

        /* 반복주기 */
        if (searchDto.getRpttIntvTypeCd() != null) {
            builder.and(qProgram.rpttIntvTypeCd.eq(searchDto.getRpttIntvTypeCd()));
        }

        /* 특정 그룹 */
        if (searchDto.getGrpId() != null) {
            builder.and(qProgram.grpId.eq(searchDto.getGrpId()));
        }

        /* 특정 프로그램 */
        if (searchDto.getPgmId() != null) {
            builder.and(qProgram.pgmId.eq(searchDto.getPgmId()));
        }

        return builder;
    }
}
