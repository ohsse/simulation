package com.hscmt.simulation.group.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.GroupType;
import com.hscmt.common.util.QProjectionUtil;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.group.GroupBy.list;

@Schema(description = "그룹정보")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupDto extends BaseDto {
    @Schema(description = "그룹_ID")
    private String grpId;
    @Schema(description = "그룹명")
    private String grpNm;
    @Schema(description = "그룹설명")
    private String grpDesc;
    @Schema(description = "정렬순서")
    private Integer sortOrd;
    @Schema(description = "상위그룹_ID")
    private String upGrpId;
    @Schema(description = "하위그룹목록")
    private List<GroupDto> children = new ArrayList<>();
    @Schema(description = "항목목록")
    private List<GroupItemDto> items = new ArrayList<>();

    public static List<Expression<?>> projectionFields(EntityPathBase<?> q) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(q),
                List.of(
                        Expressions.path(String.class, q, "grpId"),
                        Expressions.path(String.class, q, "grpNm"),
                        Expressions.path(String.class, q, "grpDesc"),
                        Expressions.path(Integer.class, q, "sortOrd"),
                        Expressions.path(String.class, q, "upGrpId")
                )
        );
    }

    public static List<Expression<?>> projectionFieldsWithChildren(EntityPathBase<?> q) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(q),
                List.of(
                        list (
                                QProjectionUtil.toQBean(GroupDto.class, GroupDto.projectionFields(q))
                        ).as("children")
                )
        );
    }

    public static List<Expression<?>> projectionFieldsWithItems (EntityPathBase<?> q, GroupType groupType) {
        return QProjectionUtil.getCombinedExpressions(
                BaseDto.getBaseFields(q),
                List.of(
                        Expressions.path(String.class, q, "grpId"),
                        Expressions.path(String.class, q, "grpNm"),
                        Expressions.path(String.class, q, "grpDesc"),
                        Expressions.path(Integer.class, q, "sortOrd"),
                        Expressions.path(String.class, q, "upGrpId"),
                        list (
                                GroupItemDto.toQBean(groupType)
                        ).as("items")
                )
        );
    }
}
