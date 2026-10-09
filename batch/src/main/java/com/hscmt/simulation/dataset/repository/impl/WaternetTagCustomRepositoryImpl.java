package com.hscmt.simulation.dataset.repository.impl;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.hscmt.simulation.dataset.domain.WaternetTag;
import com.hscmt.simulation.dataset.dto.WaternetTagDto;
import com.hscmt.simulation.dataset.repository.WaternetTagCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class WaternetTagCustomRepositoryImpl implements WaternetTagCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;
    @Override
    public List<WaternetTag> findAllTagsByIds(Set<String> tagSns) {
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;
        return queryFactory.selectFrom(qWaternetTag).where(qWaternetTag.tagSn.in(tagSns)).fetch();
    }

    @Override
    public List<WaternetTagDto> findAllWaternetTags(WaternetTagDto dto) {
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;

        BooleanBuilder builder = new BooleanBuilder();

        if (dto != null) {
            if (dto.getUseYn() != null) {
                builder.and(qWaternetTag.useYn.eq(dto.getUseYn()));
            }

            if (dto.getTagSn() != null && !dto.getTagSn().isEmpty()) {
                builder.and(qWaternetTag.tagSn.eq(dto.getTagSn()));
            }

            if (dto.getTagSeCd() != null && !dto.getTagSeCd().isEmpty()) {
                builder.and(qWaternetTag.tagSeCd.eq(dto.getTagSeCd()));
            }

        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(WaternetTagDto.class, WaternetTagDto.projectionFields(qWaternetTag))
                )
                .from(qWaternetTag)
                .where(builder)
                .fetch();
    }
}
