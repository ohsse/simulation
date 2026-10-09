package com.hscmt.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "기본 결과 dto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDto implements Serializable {
    @Schema(description = "등록아이디")
    private String rgstId;
    @Schema(description = "등록일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime rgstDttm;
    @Schema(description = "수정아이디")
    private String mdfId;
    @Schema(description = "수정일시")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime mdfDttm;

    public static List<Expression<?>> getBaseFields(EntityPathBase<?> q) {
        return List.of(
                Expressions.path(String.class, q, "rgstId"),
                Expressions.path(LocalDateTime.class, q, "rgstDttm"),
                Expressions.path(String.class, q, "mdfId"),
                Expressions.path(LocalDateTime.class, q, "mdfDttm")
        );
    }

    public void setRegisterInfo (String registerId) {
        this.rgstId = registerId;
        this.rgstDttm = LocalDateTime.now();
        this.mdfId = registerId;
        this.mdfDttm = LocalDateTime.now();
    }
}
