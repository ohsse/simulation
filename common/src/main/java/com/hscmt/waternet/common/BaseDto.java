package com.hscmt.waternet.common;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "기본정보")
public class BaseDto {
    @Schema(description = "권역본부코드")
    private String distrCd;
    @Schema(description = "권역본부명")
    private String distrNm;
    @Schema(description = "관리단코드")
    private String mgcCd;
    @Schema(description = "관리단명")
    private String mgcNm;

    public static List<Expression<?>> getBaseFields(EntityPathBase<?> q) {
        return List.of(
                Expressions.stringPath(q, "distrCd"),
                Expressions.stringPath(q, "distrNm"),
                Expressions.stringPath(q, "mgcCd"),
                Expressions.stringPath(q, "mgcNm")
        );
    }
}
