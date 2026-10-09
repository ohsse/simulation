package com.hscmt.simulation.dataset.dto.measure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "계측데이터셋 추가|수정")
public class MeasureDatasetUpsertDto extends DatasetUpsertDto {
    @Schema(description = "실시간여부", implementation = YesOrNo.class)
    private YesOrNo rltmYn;
    @Schema(description = "시작일시", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime strtDttm;
    @Schema(description = "종료일시", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDttm;
    @Schema(description = "생성주기", implementation = CycleCd.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private CycleCd termTypeCd;
    @Schema(description = "조회기간", example = "180", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer inqyTerm;
    @Schema(description = "계측항목목록")
    private List<MeasureDatasetDetailUpsertDto> detailItems = new ArrayList<>();
}
