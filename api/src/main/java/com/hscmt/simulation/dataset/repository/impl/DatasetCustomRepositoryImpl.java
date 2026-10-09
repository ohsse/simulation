package com.hscmt.simulation.dataset.repository.impl;

import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.dataset.domain.*;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetDto;
import com.hscmt.simulation.dataset.repository.DatasetCustomRepository;
import com.hscmt.simulation.group.domain.QDatasetGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;


@Repository
@RequiredArgsConstructor
@Slf4j
public class DatasetCustomRepositoryImpl implements DatasetCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;
    private final VirtualEnvironmentComponent vComp;

    @Override
    public DatasetDto findDatasetDtoById(String dsId, DatasetType dsTypeCd) {
        DatasetDto datasetDto = null;
        switch (dsTypeCd) {
            case MEASURE -> datasetDto = findMeasureDatasetDtoById(dsId);
            case USER_DEF -> datasetDto = findUserDefinitionDatasetDtoById(dsId);
            case PIPE_NETWORK -> datasetDto = findPipeNetworkDatasetDtoById(dsId);
        }
        setFileList(datasetDto);
        return datasetDto;
    }

    @Override
    public Dataset findDatasetById(String dsId, DatasetType dsTypeCd) {
        return switch (dsTypeCd) {
            case MEASURE -> queryFactory.selectFrom(QMeasureDataset.measureDataset).where(QMeasureDataset.measureDataset.dsId.eq(dsId)).fetchOne();
            case USER_DEF -> queryFactory.selectFrom(QUserDefinitionDataset.userDefinitionDataset).where(QUserDefinitionDataset.userDefinitionDataset.dsId.eq(dsId)).fetchOne();
            case PIPE_NETWORK -> queryFactory.selectFrom(QPipeNetworkDataset.pipeNetworkDataset).where(QPipeNetworkDataset.pipeNetworkDataset.dsId.eq(dsId)).fetchOne();
        };
    }

    @Override
    public List<DatasetDto> findAllDatasets(DatasetSearchDto dto) {
        DatasetType dsTypeCd = dto.getDsTypeCd();

        List<DatasetDto> returnList ;

        switch (dsTypeCd) {
            case MEASURE -> returnList = new ArrayList<>(findMeasureDatasets(dto));
            case PIPE_NETWORK -> returnList = new ArrayList<>(findPipeNetworkDatasets(dto));
            case USER_DEF -> returnList = new ArrayList<>(findUserDefinitionDatasets(dto));
            case null -> {
                List<DatasetDto> resultList = new ArrayList<>();
                resultList.addAll(findMeasureDatasets(dto));
                resultList.addAll(findPipeNetworkDatasets(dto));
                resultList.addAll(findUserDefinitionDatasets(dto));

                returnList = resultList;
            }
        }
        /* 파일목록 세팅하고 리턴 */
        setFileList(returnList);

        return returnList;
    }

    private void setFileList(List<DatasetDto> datasetDtoList) {
        for (DatasetDto datasetDto : datasetDtoList) {
            setFileList(datasetDto);
        }
    }

    private void setFileList (DatasetDto datasetDto) {
        String datasetDirPath = FileUtil.getDirPath(vComp.getDatasetBasePath(), datasetDto.getDsId());
        List<File> files = FileUtil.getOnlyFilesInDirectory(datasetDirPath);

        if (files != null && !files.isEmpty()) {
            List<FileInfoDto> fileList = files
                    .stream()
                    .map(file -> {
                        FileInfoDto fileInfoDto = new FileInfoDto();
                        fileInfoDto.setFullFileName(file.getName());
                        fileInfoDto.setFileExtension(FileUtil.getFileExtension(file.getName()));
                        fileInfoDto.setFileNm(FileUtil.getFileNameWithoutExtension(file.getName()));
                        fileInfoDto.setFileUrl(FileUtil.getUrlPath(file.getName(), vComp.getDatasetBasePath().replace(vComp.getFileServerBasePath(), ""), datasetDto.getDsId()));
                        return fileInfoDto;
                    })
                    .toList();

            datasetDto.setFileList(fileList);
        }
    }


    @Override
    public void updateGrpIdToNull(String grpId) {
        QDataset qDataset = QDataset.dataset;
        queryFactory.update(qDataset)
                .set(qDataset.grpId, (String) null)
                .where(qDataset.grpId.eq(grpId))
                .execute();
    }

    @Override
    public void updateGrpIdToNull(List<String> grpIds) {
        QDataset qDataset = QDataset.dataset;
        queryFactory.update(qDataset)
                .set(qDataset.grpId, (String) null)
                .where(qDataset.grpId.in(grpIds))
                .execute();
    }

    public List<MeasureDatasetDto> findMeasureDatasets(DatasetSearchDto dto) {
        QMeasureDataset qMeasureDataset = QMeasureDataset.measureDataset;
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;
        QDatasetGroup qDatasetGroup = QDatasetGroup.datasetGroup;
        return queryFactory
                .selectFrom(qMeasureDataset)
                .leftJoin(qMeasureDatasetDetail).on(qMeasureDataset.dsId.eq(qMeasureDatasetDetail.dataset.dsId)).fetchJoin()
                .leftJoin(qWaternetTag).on(qMeasureDatasetDetail.tagSn.eq(qWaternetTag.tagSn)).fetchJoin()
                .leftJoin(qDatasetGroup).on(qMeasureDataset.grpId.eq(qDatasetGroup.grpId))
                .where(DatasetSearchDto.getConditionByDto(qMeasureDataset._super,dto))
                .orderBy(qDatasetGroup.sortOrd.asc().nullsLast(), qMeasureDataset.sortOrd.asc().nullsLast(), qMeasureDataset.dsId.asc(), qMeasureDatasetDetail.sortOrd.asc().nullsLast())
                .transform(
                        groupBy(qMeasureDataset.dsId)
                                .list(
                                        QProjectionUtil.toQBean(MeasureDatasetDto.class, MeasureDatasetDto.projectionFieldsWithDetailList(qMeasureDataset, qMeasureDatasetDetail, qWaternetTag))
                                )
                )

                ;
    }

    public List<PipeNetworkDatasetDto> findPipeNetworkDatasets(DatasetSearchDto dto) {
        QPipeNetworkDataset qPipeNetworkDataset = QPipeNetworkDataset.pipeNetworkDataset;
        QDatasetGroup qDatasetGroup = QDatasetGroup.datasetGroup;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(PipeNetworkDatasetDto.class, PipeNetworkDatasetDto.projectionFields(qPipeNetworkDataset))
                )
                .from(qPipeNetworkDataset)
                .leftJoin(qDatasetGroup).on(qPipeNetworkDataset.grpId.eq(qDatasetGroup.grpId))
                .where(DatasetSearchDto.getConditionByDto(qPipeNetworkDataset._super,dto))
                .orderBy(qDatasetGroup.sortOrd.asc().nullsLast(), qPipeNetworkDataset.sortOrd.asc().nullsLast())
                .fetch();
    }

    public List<UserDefinitionDatasetDto> findUserDefinitionDatasets(DatasetSearchDto dto) {
        QUserDefinitionDataset qUserDefinitionDataset = QUserDefinitionDataset.userDefinitionDataset;
        QDatasetGroup qDatasetGroup = QDatasetGroup.datasetGroup;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(UserDefinitionDatasetDto.class, UserDefinitionDatasetDto.projectionFields(qUserDefinitionDataset))
                )
                .from(qUserDefinitionDataset)
                .leftJoin(qDatasetGroup).on(qUserDefinitionDataset.grpId.eq(qDatasetGroup.grpId))
                .where(DatasetSearchDto.getConditionByDto(qUserDefinitionDataset._super,dto))
                .orderBy(qDatasetGroup.sortOrd.asc().nullsLast(), qUserDefinitionDataset.sortOrd.asc().nullsLast())
                .fetch();
    }

    public MeasureDatasetDto findMeasureDatasetDtoById(String dsId) {
        QMeasureDataset qMeasureDataset = QMeasureDataset.measureDataset;
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;
        MeasureDatasetDto datasetDto = queryFactory
                .selectFrom(qMeasureDataset)
                .leftJoin(qMeasureDatasetDetail).on(qMeasureDataset.dsId.eq(qMeasureDatasetDetail.dataset.dsId)).fetchJoin()
                .leftJoin(qWaternetTag).on(qMeasureDatasetDetail.tagSn.eq(qWaternetTag.tagSn)).fetchJoin()
                .where(qMeasureDataset.dsId.eq(dsId))
                .orderBy(qMeasureDataset.dsId.asc(), qMeasureDatasetDetail.sortOrd.asc())
                .transform(
                        groupBy(qMeasureDataset.dsId)
                                .list(
                                        QProjectionUtil.toQBean(MeasureDatasetDto.class, MeasureDatasetDto.projectionFieldsWithDetailList(qMeasureDataset, qMeasureDatasetDetail, qWaternetTag))
                                )
                )
                .stream().findFirst().orElse(null);

        if (datasetDto != null) {
            List<MeasureDatasetDetailDto> details = datasetDto.getDetailItems();

            if (details.size() == 1) {
                if (details.stream().findFirst().get().getRgstDttm() == null) {
                    datasetDto.setDetailItems(new ArrayList<>());
                }
            }

        }
        return datasetDto;
    }

    public DatasetDto findUserDefinitionDatasetDtoById(String dsId) {
        QUserDefinitionDataset qUserDefinitionDataset = QUserDefinitionDataset.userDefinitionDataset;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(UserDefinitionDatasetDto.class, UserDefinitionDatasetDto.projectionFields(qUserDefinitionDataset))
                )
                .from(qUserDefinitionDataset)
                .where(qUserDefinitionDataset.dsId.eq(dsId))
                .fetchOne();
    }

    public DatasetDto findPipeNetworkDatasetDtoById(String dsId) {
        QPipeNetworkDataset qPipeNetworkDataset = QPipeNetworkDataset.pipeNetworkDataset;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(PipeNetworkDatasetDto.class, PipeNetworkDatasetDto.projectionFields(qPipeNetworkDataset))
                )
                .from(qPipeNetworkDataset)
                .where(qPipeNetworkDataset.dsId.eq(dsId))
                .fetchOne();
    }
}
