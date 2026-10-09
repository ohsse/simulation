package com.hscmt.waternet.wro.repository;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.wro.domain.QWideCustomerUsage;
import com.hscmt.waternet.wro.dto.SearchWideCustomerUsageDto;
import com.hscmt.waternet.wro.dto.WideCustomerUsageDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WroRepository {
    
    @Qualifier("waternetQueryFactory")
    private final JPAQueryFactory queryFactory;

    public List<WideCustomerUsageDto> findAllWideCustomerUsage (SearchWideCustomerUsageDto searchDto) {
        QWideCustomerUsage qWideCustomerUsage = QWideCustomerUsage.wideCustomerUsage;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        BooleanBuilder builder = new BooleanBuilder();

        if (searchDto.getSearchYyyyMm() != null) {
            builder.and(qWideCustomerUsage.key.useYm.eq(formatter.format(searchDto.getSearchYyyyMm())));
        }

        if (searchDto.getStartUsage() != null) {
            builder.and(qWideCustomerUsage.mtUsgqty.goe(searchDto.getStartUsage()));
        }

        if (searchDto.getEndUsage() != null) {
            builder.and(qWideCustomerUsage.mtUsgqty.loe(searchDto.getEndUsage()));
        }

        if (searchDto.getKeyword() != null) {
            builder.and(qWideCustomerUsage.cstmrNm.like("%" +  searchDto.getKeyword() + "%"));
        }

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(WideCustomerUsageDto.class, WideCustomerUsageDto.projectionFields(qWideCustomerUsage))
                )
                .from(qWideCustomerUsage)
                .where(builder)
                .fetch();
    }
}
