package com.hscmt.simulation.dataset.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QMeasureDatasetDetail;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.repository.MeasureDatasetDetailCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeasureDatasetDetailCustomRepositoryImpl implements MeasureDatasetDetailCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllByDsId(String dsId) {
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        queryFactory
                .delete(qMeasureDatasetDetail)
                .where(qMeasureDatasetDetail.dataset.dsId.eq(dsId))
                .execute();
    }

    @Override
    public List<MeasureDatasetDetailDto> findAllByDsId(String dsId) {
        QMeasureDatasetDetail qMeasureDatasetDetail = QMeasureDatasetDetail.measureDatasetDetail;
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(MeasureDatasetDetailDto.class, MeasureDatasetDetailDto.projectionFields(qMeasureDatasetDetail, qWaternetTag))
                )
                .from(qMeasureDatasetDetail)
                .join(qWaternetTag).on(qMeasureDatasetDetail.tagSn.eq(qWaternetTag.tagSn)).fetchJoin()
                .where(qMeasureDatasetDetail.dataset.dsId.eq(dsId))
                .fetch();
    }
}
