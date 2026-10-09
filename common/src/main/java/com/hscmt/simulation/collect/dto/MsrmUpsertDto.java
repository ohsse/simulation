package com.hscmt.simulation.collect.dto;

import com.hscmt.common.dto.BaseDto;
import com.hscmt.waternet.tag.dto.TagDataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MsrmUpsertDto extends BaseDto {
    @Schema(description = "태그번호")
    private String tagSn;
    @Schema(description = "계측일시")
    private LocalDateTime msrmDttm;
    @Schema(description = "계측값")
    private BigDecimal msrmVal;

    public MsrmUpsertDto (TagDataDto dto, String executorId) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        this.tagSn = String.valueOf(dto.getTagsn());
        this.msrmDttm = LocalDateTime.parse(dto.getLogTime(), formatter);
        this.msrmVal = dto.getVal();
        super.setRegisterInfo(executorId);
    }
}
