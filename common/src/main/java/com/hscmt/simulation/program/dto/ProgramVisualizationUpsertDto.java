package com.hscmt.simulation.program.dto;

import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.simulation.program.dto.vis.VisSetupItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "프로그램 시각화 추가|수정")
@NoArgsConstructor
@Data
public class ProgramVisualizationUpsertDto {
    @Schema(description = "프로그램시각화_ID")
    private String visId;
    @Schema(description = "프로그램시각화명")
    private String visNm;
    @Schema(description = "프로그램_ID")
    private String pgmId;
    @Schema(description = "시각화유형", implementation = VisTypeCd.class)
    private VisTypeCd visTypeCd;
    @Schema(description = "시각화설정")
    private VisSetupItem visSetupText;
//    private Map<String, Object> visSetupText;
}
