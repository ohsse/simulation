package com.hscmt.simulation.program.repository.impl;

import com.hscmt.common.enumeration.InputFileType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.program.domain.ProgramInputFile;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.domain.QProgramInputFile;
import com.hscmt.simulation.program.domain.QProgramResult;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;
import com.hscmt.simulation.program.repository.ProgramInputFileCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;


@Repository
@RequiredArgsConstructor
public class ProgramInputFileCustomRepositoryImpl implements ProgramInputFileCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPQLQueryFactory queryFactory;

    @Override
    public void deleteAllByPgmId(String pgmId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        queryFactory
                .delete(qProgramInputFile)
                .where(qProgramInputFile.pgmId.eq(pgmId))
                .execute();
    }

    @Override
    public List<ProgramInputFile> findAllByPgmId(String pgmId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        return queryFactory
                .selectFrom(qProgramInputFile)
                .where(qProgramInputFile.pgmId.eq(pgmId))
                .fetch();
    }

    @Override
    public List<ProgramInputFile> findAllByDsId(String dsId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        return queryFactory
                .selectFrom(qProgramInputFile)
                .where(qProgramInputFile.trgtType.eq(InputFileType.DATASET).and(qProgramInputFile.trgtId.eq(dsId)))
                .fetch();
    }

    @Override
    public List<ProgramInputFile> findAllByRsltId(String rsltId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        return queryFactory
                .selectFrom(qProgramInputFile)
                .where(qProgramInputFile.trgtType.eq(InputFileType.PROGRAM_RESULT).and(qProgramInputFile.trgtId.eq(rsltId)))
                .fetch();
    }

    @Override
    public List<DatasetDto> groupingForDataset(DatasetSearchDto dto) {
        QDataset qDataset = QDataset.dataset;
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QProgram qProgram = QProgram.program;
        return queryFactory
                .selectFrom(qProgramInputFile)
                .join(qDataset).on(qProgramInputFile.trgtType.eq(InputFileType.DATASET).and(qProgramInputFile.trgtId.eq(qDataset.dsId))).fetchJoin()
                .join(qProgram).on(qProgramInputFile.pgmId.eq(qProgram.pgmId)).fetchJoin()
                .where(DatasetSearchDto.getConditionByDto(qDataset, dto))
                .orderBy(qDataset.dsId.asc())
                .transform(
                        groupBy(qDataset.dsId)
                                .list(
                                        QProjectionUtil.toQBean(DatasetDto.class, DatasetDto.projectionFields(qDataset, qProgram))
                                )
                );

    }

    @Override
    public List<ProgramInputFileDto> findAllDatasetInputFiles(String pgmId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QDataset qDataset = QDataset.dataset;

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qDataset))
                )
                .from(qProgramInputFile)
                .join(qDataset).on(qProgramInputFile.trgtType.eq(InputFileType.DATASET).and(qProgramInputFile.trgtId.eq(qDataset.dsId))).fetchJoin()
                .where(setBorderCondition(qProgramInputFile, pgmId))
                .fetch();
    }

    @Override
    public List<ProgramInputFileDto> findAllResultInputFiles(String pgmId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QProgramResult qProgramResult = QProgramResult.programResult;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qProgramResult))
                )
                .from(qProgramInputFile)
                .join(qProgramResult).on(qProgramInputFile.trgtType.eq(InputFileType.PROGRAM_RESULT).and(qProgramInputFile.trgtId.eq(qProgramResult.rsltId))).fetchJoin()
                .where(setBorderCondition(qProgramInputFile, pgmId))
                .fetch();
    }

    @Override
    public List<ProgramInputFileDto> findAllDatasetInputFilesByGrpId(String grpId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QDataset qDataset = QDataset.dataset;
        QProgram qProgram = QProgram.program;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(
                    JPAExpressions
                            .selectOne()
                            .from(qProgram)
                            .where(
                                    qProgram.pgmId.eq(qProgramInputFile.pgmId)
                                            .and(qProgram.grpId.eq(grpId))
                            )
                            .exists()
            );
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qDataset))
                )
                .from(qProgramInputFile)
                .join(qDataset).on(qProgramInputFile.trgtType.eq(InputFileType.DATASET).and(qProgramInputFile.trgtId.eq(qDataset.dsId))).fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public List<ProgramInputFileDto> findAllResultInputFilesByGrpId(String grpId) {
        QProgramInputFile qProgramInputFile = QProgramInputFile.programInputFile;
        QProgramResult qProgramResult = QProgramResult.programResult;
        QProgram qProgram = QProgram.program;

        BooleanBuilder builder = new BooleanBuilder();
        if (grpId != null) {
            builder.and(
                    JPAExpressions
                            .selectOne()
                            .from(qProgram)
                            .where(
                                    qProgram.pgmId.eq(qProgramInputFile.pgmId)
                                            .and(qProgram.grpId.eq(grpId))
                            )
                            .exists()
            );
        }
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(ProgramInputFileDto.class, ProgramInputFileDto.projectionFields(qProgramInputFile, qProgramResult))
                )
                .from(qProgramInputFile)
                .join(qProgramResult).on(qProgramInputFile.trgtType.eq(InputFileType.PROGRAM_RESULT).and(qProgramInputFile.trgtId.eq(qProgramResult.rsltId))).fetchJoin()
                .where(builder)
                .fetch();
    }

    private BooleanBuilder setBorderCondition(QProgramInputFile qProgramInputFile, String pgmId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (pgmId != null) {
            builder.and(qProgramInputFile.pgmId.eq(pgmId));
        }
        return builder;
    }
}
