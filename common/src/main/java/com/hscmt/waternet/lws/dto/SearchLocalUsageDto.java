package com.hscmt.waternet.lws.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@Schema(description = "지방검침량조회")
public class SearchLocalUsageDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM")
    @Schema(description = "검색년월", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private YearMonth searchYyyyMm;
    @Schema(description = "시작사용량", example = "0")
    private Double startUsage;
    @Schema(description = "종료사용량", example = "100")
    private Double endUsage;
    @Schema(description = "검색어")
    private String keyword;
}
