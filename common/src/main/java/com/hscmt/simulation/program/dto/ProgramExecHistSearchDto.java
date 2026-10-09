package com.hscmt.simulation.program.dto;

import com.hscmt.common.dto.FromToSearchDto;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.enumeration.ExecutionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Schema(description = "프로그램이력조회")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProgramExecHistSearchDto extends FromToSearchDto {
    @Schema(description = "실행유형코드", implementation = ExecutionType.class)
    private ExecutionType execTypeCd;
    @Schema(description = "실행상태코드", implementation = ExecStat.class)
    private ExecStat execSttsCd;
}
