package com.hscmt.waternet.tag.repository;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.DateTimeUtil;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.waternet.tag.domain.QIfTag;
import com.hscmt.waternet.tag.domain.RwisData;
import com.hscmt.waternet.tag.domain.child.QRwisHourData;
import com.hscmt.waternet.tag.domain.child.QRwisMinuteData;
import com.hscmt.waternet.tag.domain.child.RwisMinuteData;
import com.hscmt.waternet.tag.dto.TagDto;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TagRepository {

    @Qualifier("waternetQueryFactory")
    private final JPAQueryFactory queryFactory;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public List<TagDto> findAllWaternetTags () {
        QIfTag qIfTag = QIfTag.ifTag;

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(TagDto.class, TagDto.projectionFields(qIfTag))
                )
                .from(qIfTag)
                .where(qIfTag.useYn.eq(YesOrNo.Y).and(qIfTag.tagSeCd.isNotNull()))
                .fetch();
    }

    public List<Map<String, Object>> getWaternetTrendData (TrendSearchDto dto) {
        QRwisMinuteData qRwisMinuteData = QRwisMinuteData.rwisMinuteData;
        QRwisHourData qRwisHourData = QRwisHourData.rwisHourData;

        String startLogTime = dto.getSearchStrtDttm().format(formatter);
        String endLogTime = dto.getSearchEndDttm().format(formatter);

        List<TagDto> tagList = dto.getTagList();

        List<TagDto> minuteTags = tagList.stream()
                .filter(tag -> !tag.getTagSeCd().endsWith("D"))
                .toList();

        List<TagDto> hourTags = tagList.stream()
                .filter(tag -> tag.getTagSeCd().endsWith("D"))
                .toList();

        List<Tuple> minuteData = new ArrayList<>();
        if (minuteTags.size() != 0) {
            minuteData = getTupleData (qRwisMinuteData, minuteTags, startLogTime, endLogTime);
        }
        List<Tuple> hourData = new ArrayList<>();
        if (hourTags.size() != 0) {
            hourData = getTupleData(qRwisHourData, hourTags, startLogTime, endLogTime);
        }

        Map<String, Map<String, Object>> logTimeMap = new LinkedHashMap<>();
        CycleCd cycleCd = dto.getCycleCd();
        List<LocalDateTime> targetTimes = DateTimeUtil.getDateTimeList(dto.getSearchStrtDttm(), dto.getSearchEndDttm(), cycleCd.getUnit());
        List<String> logTimes = targetTimes.stream().map(time -> time.format(formatter)).toList();

        logTimes.forEach(logTime -> logTimeMap.put(logTime, new LinkedHashMap<>()));

        convertTupleToMap(logTimeMap, qRwisMinuteData, minuteData, minuteTags);
        convertTupleToMap(logTimeMap, qRwisHourData, hourData, hourTags);

        logTimeMap.forEach((logTime, dataMap) -> {
            dataMap.put("logTime", logTime);
        });

        return new ArrayList<>(logTimeMap.values());
    }

    private void convertTupleToMap (Map<String, Map<String, Object>> logTimeMap, EntityPathBase<?> q, List<Tuple> tupleList, List<TagDto> tagList) {
        StringPath logTime = Expressions.stringPath(q, "id.logTime");
        for (Tuple tuple : tupleList) {
            String logTimeStr = tuple.get(logTime);
            Map<String, Object> dataMap = logTimeMap.get(logTimeStr);
            if (dataMap != null) {
                for (int i = 0; i < tagList.size(); i++) {
                    TagDto tag =  tagList.get(i);
                    dataMap.put(tag.getTagSn(), tuple.get(Expressions.numberPath(BigDecimal.class, "id_" + i)));
                }

                logTimeMap.put(logTimeStr, dataMap);
            }
        }
    }


    private List<Tuple> getTupleData (EntityPathBase<?> q, List<TagDto> tagList, String startLogTime, String endLogTime) {
        StringPath tagsn = Expressions.stringPath(q, "id.tagsn");
        StringPath logTime = Expressions.stringPath(q, "id.logTime");
        return queryFactory
                .select(
                        getTagExpressions(q, tagList).toArray(new Expression[0])
                )
                .from(q)
                .where(
                        tagsn.in(tagList.stream().map(TagDto::getTagSn).toList())
                                .and(logTime.goe(startLogTime))
                                .and(logTime.loe(endLogTime))
                )
                .groupBy(logTime)
                .orderBy(logTime.asc())
                .fetch();
    }


    private List<Expression<?>> getTagExpressions (EntityPathBase<?> q, List<TagDto> tagList) {
        List<Expression<?>> expressions = new ArrayList<>();
        StringPath logTime = Expressions.stringPath(q, "id.logTime");
        expressions.add(logTime);
        StringPath tagsn = Expressions.stringPath(q, "id.tagsn");
        NumberPath<BigDecimal> val = Expressions.numberPath(BigDecimal.class, q, "val");

        for (int i = 0 ; i < tagList.size() ; i++) {
            TagDto tag = tagList.get(i);
            NumberExpression<BigDecimal> tagValueExpressions = new CaseBuilder()
                    .when(tagsn.eq(tag.getTagSn()))
                    .then(val)
                    .otherwise((BigDecimal) null);
            expressions.add(tagValueExpressions.max().as("id_" + i));
        }
        return expressions;
    }
}