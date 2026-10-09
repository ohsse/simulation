package com.hscmt.simulation.venv.dto;

import com.hscmt.common.enumeration.YesOrNo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Schema(description = "가상환경 패키지 정보")
public class VenvPackageDto {
    @Schema(description = "원본파일명")
    private String ortxFileNm;
    @Schema(description = "폴더여부")
    private Boolean isDir;
    @Schema(description = "파일경로")
    private String filePath;
    @Schema(description = "부모경로")
    private String parentPath;
    @ArraySchema(schema = @Schema(description = "자식목록", implementation = VenvPackageDto.class))
    private List<VenvPackageDto> children = new ArrayList<>();

}
