package com.hscmt.simulation.program.repository.impl;

import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.simulation.program.domain.ProgramExecHist;
import com.hscmt.simulation.program.domain.QProgramExecHist;
import com.hscmt.simulation.program.repository.ProgramExecHistCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProgramExecHistCustomRepositoryImpl implements ProgramExecHistCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;


    @Override
    public List<ProgramExecHist> findByPgmIdLimit(String pgmId, Long limitCount) {
        QProgramExecHist qProgramExecHist = QProgramExecHist.programExecHist;
        return queryFactory
                .selectFrom(qProgramExecHist)
                .where(qProgramExecHist.pgmId.eq(pgmId).and(qProgramExecHist.execSttsCd.eq(ExecStat.COMPLETED)).and(qProgramExecHist.rsltBytes.isNotNull()))
                .orderBy(qProgramExecHist.execStrtDttm.asc())
                .limit(limitCount)
                .fetch();

    }
}
