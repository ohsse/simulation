package com.hscmt.waternet.lws.repository;

import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.lws.domain.QLocalCivilApplicant;
import com.hscmt.waternet.lws.domain.QLocalCustomer;
import com.hscmt.waternet.lws.domain.QLocalCustomerUsage;
import com.hscmt.waternet.lws.dto.LocalCivilApplicantDto;
import com.hscmt.waternet.lws.dto.LocalCustomerUsageDto;
import com.hscmt.waternet.lws.dto.SearchCivilApplicantDto;
import com.hscmt.waternet.lws.dto.SearchLocalUsageDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LwsRepository {
    @Qualifier("waternetQueryFactory")
    private final JPAQueryFactory queryFactory;

    public List<LocalCustomerUsageDto> getLocalCustomerUsage (SearchLocalUsageDto searchDto) {
        QLocalCustomerUsage qLocalCustomerUsage = QLocalCustomerUsage.localCustomerUsage;
        QLocalCustomer qLocalCustomer = QLocalCustomer.localCustomer;

        BooleanBuilder builder = new BooleanBuilder();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        if (searchDto.getSearchYyyyMm() != null) {
            builder.and(qLocalCustomerUsage.oriStym.eq(formatter.format(searchDto.getSearchYyyyMm())));
        }

        String keyword = searchDto.getKeyword();

        if (keyword != null && !keyword.isBlank()){
            builder.and(qLocalCustomer.dmaddr.like("%"+keyword+"%")
                    .or(qLocalCustomer.lfcltyNm.like("%"+keyword+"%"))
                    .or(qLocalCustomer.mfcltyNm.like("%"+keyword+"%"))
                    .or(qLocalCustomer.sfcltyNm.like("%"+keyword+"%")))
                    ;
        }

        if (searchDto.getStartUsage() != null) {
            builder.and(qLocalCustomerUsage.wsstvol.goe(searchDto.getStartUsage()));
        }

        if (searchDto.getEndUsage() != null) {
            builder.and(qLocalCustomerUsage.wsstvol.loe(searchDto.getEndUsage()));
        }

       try {
           return queryFactory
                   .select(
                           QProjectionUtil.toQBean(LocalCustomerUsageDto.class, LocalCustomerUsageDto.projectionFields(qLocalCustomerUsage, qLocalCustomer))
                   )
                   .from(qLocalCustomer)
                   .join(qLocalCustomerUsage).on(qLocalCustomer.dmno.eq(qLocalCustomerUsage.key.dmno)).fetchJoin()
                   .where(builder)
                   .fetch();
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }

    public List<LocalCivilApplicantDto> getLocalCivilApplicants (SearchCivilApplicantDto searchDto) {
        QLocalCivilApplicant qLocalCivilApplicant =  QLocalCivilApplicant.localCivilApplicant;

        BooleanBuilder builder = new BooleanBuilder();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (searchDto.getStartYyyyMmDd() != null) {
            System.out.println(formatter.format(searchDto.getStartYyyyMmDd()));
            builder.and(qLocalCivilApplicant.caappldt.goe(formatter.format(searchDto.getStartYyyyMmDd())));
        }

        if (searchDto.getEndYyyyMmDd() != null) {
            builder.and(qLocalCivilApplicant.caappldt.loe(formatter.format(searchDto.getEndYyyyMmDd())));
        }

        String keyword = searchDto.getKeyword();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(qLocalCivilApplicant.caprcsrslt.like("%"+keyword+"%"));
        }

       try {
           return queryFactory
                   .select(
                           QProjectionUtil.toQBean(LocalCivilApplicantDto.class, LocalCivilApplicantDto.projectionFields(qLocalCivilApplicant))
                   )
                   .from(qLocalCivilApplicant)
                   .where(builder)
                   .fetch();
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }
}
