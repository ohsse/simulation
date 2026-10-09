package com.hscmt.waternet.lws.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Schema(description = "민원검색요청")
public class SearchCivilApplicantDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    @Schema(description = "검색시작일자", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private LocalDate startYyyyMmDd;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    @Schema(description = "검색종료일자", requiredMode = Schema.RequiredMode.REQUIRED, type = "string")
    private LocalDate endYyyyMmDd;
    @Schema(description = "검색어", example = "안내")
    private String keyword;
}
