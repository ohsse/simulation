package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "프로그램실행이력삭제")
@Data
@NoArgsConstructor
public class PgmExecHistDeleteDto {
    @Schema(description = "시작일자")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDttm;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endDttm;
    @ArraySchema(schema = @Schema(implementation = String.class, description = "프로그램ID"))
    private List<String> pgmIds = new ArrayList<>();

}
