package com.hscmt.simulation.dataset.repository;

import com.hscmt.common.util.DateTimeUtil;
import com.hscmt.simulation.dataset.domain.QMeasureList;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MeasureListRepository {
    @Qualifier("simulationQueryFactory")
    private final JPAQueryFactory queryFactory;

    public List<Map<String, Object>> findAllDatasetDataList (List<MeasureDatasetDetailDto> tagList, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Set<String> tagSns = tagList.stream().map(MeasureDatasetDetailDto::getTagSn).collect(Collectors.toSet());

        QMeasureList qMeasureList = QMeasureList.measureList;

        List<Tuple> list = queryFactory
                .select(getMeasureExpressions(tagSns).toArray(new Expression[0]))
                .from(qMeasureList)
                .where(
                        qMeasureList.id.tagSn.in(tagSns)
                                .and(qMeasureList.id.msrmDttm.goe(startDateTime))
                                .and(qMeasureList.id.msrmDttm.loe(endDateTime))
                )
                .groupBy(qMeasureList.id.msrmDttm)
                .orderBy(qMeasureList.id.msrmDttm.asc())
                .fetch();

        return convertTupleToMap (tagSns, list, startDateTime, endDateTime);
    }

    private List<Map<String, Object>> convertTupleToMap (Set<String> tagSns, List<Tuple> orgList, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        QMeasureList qMeasureList = QMeasureList.measureList;
        List<LocalDateTime> targetTimes = DateTimeUtil.getDateTimeList(startDateTime, endDateTime, ChronoUnit.MINUTES);

        List<Map<String, Object>> resultList = new ArrayList<>();

        Map<String, Map<String, Object>> map = new HashMap<>();

        for (Tuple tuple : orgList) {
            String msrmDttm = formatter.format(tuple.get(qMeasureList.id.msrmDttm));
            Map<String, Object> dataMap = map.get(msrmDttm);

            if (dataMap == null) {
                dataMap = new LinkedHashMap<>();
                map.put(msrmDttm, dataMap);
            }

            for (String tagSn : tagSns) {
                dataMap.put(tagSn, tuple.get(Expressions.numberPath(BigDecimal.class, "id_" + tagSn)));
            }
        }

        for (LocalDateTime time : targetTimes) {
           String msrmDttm = formatter.format(time);

           Map<String, Object> resultMap = new LinkedHashMap<>();
           resultMap.put("msrmDttm", msrmDttm);

           Map<String, Object> dataMap = map.get(msrmDttm);

           if (dataMap != null) {
               resultMap.putAll(dataMap);
           } else {
               for (String tagSn : tagSns) {
                   resultMap.put(tagSn, null);
               }
           }
           resultList.add(resultMap);
        }

        return resultList;
    }

    private List<Expression<?>> getMeasureExpressions(Set<String> tagSns) {
        QMeasureList qMeasureList = QMeasureList.measureList;
        List<Expression<?>> expressions = new ArrayList<>();
        expressions.add(qMeasureList.id.msrmDttm);

        for (String tagSn : tagSns) {
            NumberExpression<BigDecimal> tagValueExpressions = new CaseBuilder()
                    .when(qMeasureList.id.tagSn.eq(tagSn))
                    .then(qMeasureList.msrmVal)
                    .otherwise((BigDecimal) null);
            expressions.add(tagValueExpressions.max().as("id_" + tagSn));
        }

        return expressions;
    }
}
