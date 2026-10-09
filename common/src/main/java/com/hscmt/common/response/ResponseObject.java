package com.hscmt.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "응답")
public class ResponseObject<T> {
    @Schema(description = "응답코드")
    private String code;
    @Schema(description = "응답데이터")
    private T data;
}
