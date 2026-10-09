package com.hscmt.waternet.tag.repository;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.util.DateTimeUtil;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.waternet.tag.domain.QIfTag;
import com.hscmt.waternet.tag.domain.RwisData;
import com.hscmt.waternet.tag.domain.child.QRwisHourData;
import com.hscmt.waternet.tag.domain.child.QRwisMinuteData;
import com.hscmt.waternet.tag.dto.TagDataDto;
import com.hscmt.waternet.tag.dto.TagDto;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

    public TagDataDto findTagData (TagCollectDto dto) {
        EntityPathBase<?> q = getTargetTable (dto.getTagSeCd());
        BooleanBuilder builder = getCollectCondition (q ,dto);
        return queryFactory
                .select(
                    QProjectionUtil.toQBean(TagDataDto.class, TagDataDto.projectionFields(q))
                )
                .from(q)
                .where(builder)
                .fetchOne();
    }

    public List<TagDataDto> findTagDataList (TagCollectDto dto) {
        EntityPathBase<?> q = getTargetTable(dto.getTagSeCd());
        BooleanBuilder builder = getCollectCondition(q, dto);
        return queryFactory
                .select(
                        QProjectionUtil.toQBean(TagDataDto.class, TagDataDto.projectionFields(q))
                )
                .from(q)
                .where(builder)
                .fetch();
    }


    private EntityPathBase<?> getTargetTable (String tagSeCd) {
        EntityPathBase<?> q = QRwisMinuteData.rwisMinuteData;
        if (tagSeCd.endsWith("D")) {
            q = QRwisHourData.rwisHourData;
        }
        return q;
    }

    private BooleanBuilder getCollectCondition (EntityPathBase<?> q, TagCollectDto dto) {
        BooleanBuilder builder = new BooleanBuilder();

        StringPath logTime = Expressions.stringPath(q, "id.logTime");
        NumberPath<Long> tagsn = Expressions.numberPath(Long.class,q, "id.tagsn");

        if (dto.getTagsn() != null && !dto.getTagsn().isEmpty()) {
            builder.and(tagsn.eq(Long.parseLong(dto.getTagsn())));
        }

        if (dto.getTargetLogTime() != null && !dto.getTargetLogTime().isEmpty()) {
            builder.and(logTime.eq(dto.getTargetLogTime()));
        }

        if (dto.getStartLogTime() != null && !dto.getStartLogTime().isEmpty()) {
            builder.and(logTime.goe(dto.getStartLogTime()));
        }

        if (dto.getEndLogTime() != null && !dto.getEndLogTime().isEmpty()) {
            builder.and(logTime.loe(dto.getEndLogTime()));
        }

        return builder;
    }

    public List<TagDto> findAllWaternetTags () {
        QIfTag qIfTag = QIfTag.ifTag;

        queryFactory
                .select(
                        QProjectionUtil.toQBean(TagDto.class, TagDto.projectionFields(qIfTag))
                )
                .from(qIfTag)
                .where(qIfTag.useYn.eq(YesOrNo.Y));

        return queryFactory
                .select(
                        QProjectionUtil.toQBean(TagDto.class, TagDto.projectionFields(qIfTag))
                )
                .from(qIfTag)
                .where(qIfTag.useYn.eq(YesOrNo.Y))
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
                tagList.forEach(tag -> {
                    dataMap.put(tag.getTagSn(), tuple.get(Expressions.numberPath(BigDecimal.class, "id_" + tag.getTagSn())));
                });
                logTimeMap.put(logTimeStr, dataMap);
            }
        }
    }


    private List<Tuple> getTupleData (EntityPathBase<?> q, List<TagDto> tagList, String startLogTime, String endLogTime) {
        NumberPath<Long> tagsn = Expressions.numberPath(Long.class,q, "id.tagsn");
        StringPath logTime = Expressions.stringPath(q, "id.logTime");
        return queryFactory
                .select(
                        getTagExpressions(q, tagList).toArray(new Expression[0])
                )
                .from(q)
                .where(
                        tagsn.in(tagList.stream().map(x -> Long.parseLong(x.getTagSn())).toList())
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
        NumberPath<Long> tagsn = Expressions.numberPath(Long.class, q, "id.tagsn");
        NumberPath<BigDecimal> val = Expressions.numberPath(BigDecimal.class, q, "val");

        tagList.forEach(tag -> {
            NumberExpression<BigDecimal> tagValueExpressions = new CaseBuilder()
                    .when(tagsn.eq(Long.parseLong(tag.getTagSn())))
                    .then(val)
                    .otherwise((BigDecimal) null);

            expressions.add(tagValueExpressions.max().as("id_" + tag.getTagSn()));
        });

        return expressions;
    }


}
