package com.hscmt.simulation.collect.repository;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QWaternetTag;
import com.hscmt.simulation.dataset.dto.WaternetTagDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TagCollectRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    public List<WaternetTagDto> findAllCollectTag (List<String> tagSns) {
        QWaternetTag qWaternetTag = QWaternetTag.waternetTag;

        BooleanBuilder builder = new BooleanBuilder();

        if (tagSns != null && tagSns.size() > 0) {
            builder.and(qWaternetTag.tagSn.in(tagSns));
        }

        builder.and(qWaternetTag.useYn.eq(YesOrNo.Y));

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(WaternetTagDto.class, WaternetTagDto.projectionFields(qWaternetTag))
                )
                .from(qWaternetTag)
                .where(builder)
                .fetch();
    }
}
