package com.hscmt.waternet.tag.dto;

import com.hscmt.common.dto.FromToSearchDto;
import com.hscmt.common.enumeration.CycleCd;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "워터넷 트랜드 조회")
public class TrendSearchDto extends FromToSearchDto {
    @Schema(description = "계측주기", implementation = CycleCd.class)
    private CycleCd cycleCd;
    @ArraySchema(schema = @Schema(description = "태그정보", implementation = TagDto.class))
    private List<TagDto> tagList = new ArrayList<>();
}
