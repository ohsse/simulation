package com.hscmt.simulation.dataset.repository.impl;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.util.DateTimeUtil;
import com.hscmt.simulation.dataset.domain.QMeasureList;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureSearchDto;
import com.hscmt.simulation.dataset.repository.MeasureListCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MeasureListCustomRepositoryImpl implements MeasureListCustomRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    /* 계측데이터 조회 */
    @Override
    public List<Map<String, Object>> findMeasureList(List<MeasureDatasetDetailDto> targetTags, MeasureSearchDto dto) {
        Set<String> tagSns = targetTags.stream().map(MeasureDatasetDetailDto::getTagSn).collect(Collectors.toSet());

        QMeasureList qMeasureList = QMeasureList.measureList;

        List<Tuple> list = queryFactory
                .select(getMeasureExpressions(tagSns).toArray(new Expression[0]))
                .from(qMeasureList)
                .where(getMeasureBuilder(tagSns, dto))
                .groupBy(qMeasureList.id.msrmDttm)
                .orderBy(qMeasureList.id.msrmDttm.asc())
                .fetch();

        return convertTupleToMap (tagSns, list, dto);
    }

    /* 표현식으로 바꾼 데이터를 map으로 변경 */
    private List<Map<String, Object>> convertTupleToMap (Set<String> tagSns, List<Tuple> orgList, MeasureSearchDto dto) {
        QMeasureList qMeasureList = QMeasureList.measureList;
        List<LocalDateTime> targetTimes = new ArrayList<>();
        /* 계측주기 */
        CycleCd cycleCd = dto.getCyclCd();
        /* 마스터 시간축 가져오기 */
        targetTimes = DateTimeUtil.getDateTimeList(dto.getSearchStrtDttm(), dto.getSearchEndDttm(), cycleCd.getUnit());

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : orgList) {
            Map<String, Object> map = new LinkedHashMap<>();

            LocalDateTime msrmDttm = tuple.get(qMeasureList.id.msrmDttm);

            boolean isAdd = true;

            if (targetTimes.size() > 0) {
                isAdd = targetTimes.contains(msrmDttm);
            }

            if (isAdd) {
                map.put("msrmDttm", msrmDttm);
                int i = 0;
                for (String tagSn : tagSns) {
                    map.put(tagSn, tuple.get(Expressions.numberPath(BigDecimal.class, "id_" + i)));
                    i++;
                }
                resultList.add(map);
            }
        }

        return resultList;
    }



    private BooleanBuilder getMeasureBuilder (Set<String> tagSns, MeasureSearchDto dto) {
        QMeasureList qMeasureList = QMeasureList.measureList;
        BooleanBuilder builder = new BooleanBuilder();

        if (tagSns.size() > 0) {
            builder.and(qMeasureList.id.tagSn.in(tagSns));
        }

        if (dto.getSearchStrtDttm() != null) {
            builder.and(qMeasureList.id.msrmDttm.goe(dto.getSearchStrtDttm()));
        }

        if (dto.getSearchEndDttm() != null) {
            builder.and(qMeasureList.id.msrmDttm.loe(dto.getSearchEndDttm()));
        }

        return builder;
    }

    private List<Expression<?>> getMeasureExpressions(Set<String> tagSns) {
        QMeasureList qMeasureList = QMeasureList.measureList;
        List<Expression<?>> expressions = new ArrayList<>();
        expressions.add(qMeasureList.id.msrmDttm);


        int i = 0;
        for (String tagSn : tagSns) {
            NumberExpression<BigDecimal> tagValueExpressions = new CaseBuilder()
                    .when(qMeasureList.id.tagSn.eq(tagSn))
                    .then(qMeasureList.msrmVal)
                    .otherwise((BigDecimal) null);

            expressions.add(tagValueExpressions.max().as("id_" + i));
            i++;
        }

        return expressions;
    }
}
