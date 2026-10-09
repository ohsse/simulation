package com.hscmt.simulation.program.dto.vis;

import com.hscmt.common.enumeration.VisTypeCd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImageResultItemDto implements VisResultItem{
    @Schema(description = "타입", implementation = VisTypeCd.class)
    private String type = VisTypeCd.IMAGE.name();
    @Schema(description = "파일요청경로")
    private String fileUrl;

}
