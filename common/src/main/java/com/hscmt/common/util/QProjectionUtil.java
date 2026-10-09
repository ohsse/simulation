package com.hscmt.common.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryDSL Projection 유틸리티 클래스
 * <p>
 * 여러 그룹의 {@link Expression} 목록을 합쳐서
 * {@link Projections#fields(Class, Expression...)} 방식으로 QBean을 생성하는 기능을 제공합니다.
 */
public class QProjectionUtil {

    /**
     * 여러 Expression 그룹을 합쳐서 QBean 생성
     *
     * @param dtoClass         매핑할 DTO 클래스
     * @param expressionGroups 합칠 Expression 그룹(여러 List 가능)
     * @param <T>              DTO 타입
     * @return QueryDSL {@link QBean} 객체
     */
    @SafeVarargs
    public static <T> QBean<T> toQBean(Class<T> dtoClass, List<Expression<?>>... expressionGroups) {
        List<Expression<?>> combined = getCombinedExpressions(expressionGroups);
        return Projections.fields(dtoClass, combined.toArray(new Expression[0]));
    }

    /**
     * 여러 Expression 그룹을 하나의 List로 합침
     *
     * @param expressionGroups 합칠 Expression 그룹(여러 List 가능)
     * @return 합쳐진 Expression 리스트
     */
    @SafeVarargs
    public static List<Expression<?>> getCombinedExpressions(List<Expression<?>>... expressionGroups) {
        List<Expression<?>> combined = new ArrayList<>();
        for (List<Expression<?>> group : expressionGroups) {
            combined.addAll(group);
        }
        return combined;
    }
}
