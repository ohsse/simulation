package com.hscmt.simulation.program.dto;

import com.hscmt.common.dto.FromToSearchDto;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.enumeration.YesOrNo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "프로그램 실행이력 조회")
@NoArgsConstructor
@Data
public class ProgramExecSearchDto extends FromToSearchDto {
    @Schema(description = "실시간여부", implementation = YesOrNo.class)
    private YesOrNo rltmYn;
    @Schema(description = "실행반복주기유형", implementation = CycleCd.class)
    private CycleCd rpttIntvTypeCd;
    @Schema(description = "그룹ID")
    private String grpId;
    @Schema(description = "실행유형코드", implementation = ExecutionType.class)
    private ExecutionType execTypeCd;
    @Schema(description = "실행상태코드", implementation = ExecStat.class)
    private ExecStat execSttsCd;
    @Schema(description = "프로그램ID")
    private String pgmId;
}
