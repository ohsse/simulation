package com.hscmt.simulation.program.repository;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.hscmt.simulation.program.domain.*;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;
import com.hscmt.simulation.program.dto.ProgramResultDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProgramRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    public List<ProgramDto> findAllRealtimePrograms () {
        QProgram qProgram = QProgram.program;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramDto.class, ProgramDto.projectionFields(qProgram))
                )
                .from(qProgram)
                .where(qProgram.rltmYn.eq(YesOrNo.Y))
                .fetch();
    }

    public Program findProgramById (String pgmId) {
        QProgram qProgram = QProgram.program;
        return queryFactory
                .selectFrom(qProgram)
                .where(qProgram.pgmId.eq(pgmId))
                .fetchOne();
    }

    public List<ProgramResultDto> findProgramResultsByPgmId (String pgmId) {
        QProgramResult qProgramResult = QProgramResult.programResult;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramResultDto.class, ProgramResultDto.projectionFields(qProgramResult))
                )
                .from(qProgramResult)
                .where(qProgramResult.pgmId.eq(pgmId))
                .fetch();
    }

    public List<ProgramInputFileDto> findProgramInputFilesByPgmId (String pgmId) {

        List<ProgramInputFileDto> result = new ArrayList<>();


        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QDataset qDataset = QDataset.dataset;
        QProgramResult qProgramResult = QProgramResult.programResult;
     
        List<ProgramInputFileDto> ds = queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qDataset))
                )
                .from(qProgramInputFile)
                .join(qDataset).on(qProgramInputFile.trgtId.eq(qDataset.dsId)).fetchJoin()
                .where(qProgramInputFile.pgmId.eq(pgmId))
                .fetch();
        
        List<ProgramInputFileDto> pr = queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qProgramResult))
                )
                .from(qProgramInputFile)
                .join(qProgramResult).on(qProgramInputFile.trgtId.eq(qProgramResult.rsltId)).fetchJoin()
                .where(qProgramInputFile.pgmId.eq(pgmId))
                .fetch();
        
        /* 데이터셋 유형 세팅 */
        if (ds.size() > 0) result.addAll(ds);
        /* 결과파일 유형 세팅 */
        if (pr.size() > 0) result.addAll(pr);
        return result;
    }

    public ProgramResultDto findPgmIdByProgramResultId (String rsltId) {
        QProgramResult qProgramResult = QProgramResult.programResult;

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramResultDto.class, ProgramResultDto.projectionFields(qProgramResult))
                )
                .from(qProgramResult)
                .where(qProgramResult.rsltId.eq(rsltId))
                .fetchOne();
    }
}
