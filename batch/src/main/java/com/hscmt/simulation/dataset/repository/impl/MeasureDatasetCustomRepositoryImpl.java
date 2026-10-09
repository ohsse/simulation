package com.hscmt.simulation.dataset.repository.impl;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QMeasureDataset;
import com.hscmt.simulation.dataset.domain.QMeasureDatasetDetail;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.repository.MeasureDatasetCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
@RequiredArgsConstructor
public class MeasureDatasetCustomRepositoryImpl implements MeasureDatasetCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MeasureDatasetDto> findAllDatasets(YesOrNo rltmYn) {
        QMeasureDataset qMeasureDataset = QMeasureDataset.measureDataset;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(MeasureDatasetDto.class, MeasureDatasetDto.projectionFields(qMeasureDataset))
                )
                .from(qMeasureDataset)
                .where(qMeasureDataset.rltmYn.eq(rltmYn))
                .fetch();
    }

    @Override
    public List<MeasureDatasetDto> findDatasetDetailInfoByDsId(String dsId) {
        QMeasureDataset qMeasureDataset = QMeasureDataset.measureDataset;
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;

        return queryFactory
                .selectFrom(qMeasureDataset)
                .join(qMeasureDatasetDetail).on(qMeasureDataset.dsId.eq(qMeasureDatasetDetail.dataset.dsId))
                .join(qWaternetTag).on(qMeasureDatasetDetail.tagSn.eq(qWaternetTag.tagSn))
                .where(qMeasureDataset.dsId.eq(dsId))
                .orderBy(qMeasureDatasetDetail.sortOrd.asc())
                .transform (
                        groupBy(qMeasureDataset.dsId)
                                .list(
                                        QProjectionUtil.toQBean(MeasureDatasetDto.class, MeasureDatasetDto.projectionFieldsWithDetailList(qMeasureDataset, qMeasureDatasetDetail, qWaternetTag))
                                )
                );
    }

    @Override
    public List<MeasureDatasetDto> findAllDatasetDetailInfo() {
        QMeasureDataset qMeasureDataset = QMeasureDataset.measureDataset;
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;

        return queryFactory
                .selectFrom(qMeasureDataset)
                .join(qMeasureDatasetDetail).on(qMeasureDataset.dsId.eq(qMeasureDatasetDetail.dataset.dsId))
                .join(qWaternetTag).on(qMeasureDatasetDetail.tagSn.eq(qWaternetTag.tagSn))
                .orderBy(qMeasureDatasetDetail.sortOrd.asc())
                .transform (
                        groupBy(qMeasureDataset.dsId)
                                .list(
                                        QProjectionUtil.toQBean(MeasureDatasetDto.class, MeasureDatasetDto.projectionFieldsWithDetailList(qMeasureDataset, qMeasureDatasetDetail, qWaternetTag))
                                )
                );
    }
}
