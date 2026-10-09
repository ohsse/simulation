package com.hscmt.simulation.program.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.dto.FileInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "프로그램파일정보")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ProgramFileDto extends FileInfoDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "등록일시")
    private LocalDateTime rgstDttm;
}
