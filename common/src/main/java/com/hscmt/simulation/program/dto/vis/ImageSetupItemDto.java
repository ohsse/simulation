package com.hscmt.simulation.program.dto.vis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hscmt.common.enumeration.VisTypeCd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "이미지항목")
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageSetupItemDto implements VisSetupItem{
    @Schema(description = "시각화유형", implementation = VisTypeCd.class)
    private String type = VisTypeCd.IMAGE.name();
    @Schema(description = "프로그램결과명", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String pgmRsltNm;
    @Schema(description = "파일명")
    private String fileName;
    @Override
    public String getId() {return null;}
    @Override
    public String getLabel() {return null;}
}
