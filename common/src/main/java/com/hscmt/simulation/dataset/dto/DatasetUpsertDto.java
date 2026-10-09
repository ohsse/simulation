package com.hscmt.simulation.dataset.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetUpsertDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "데이터셋 등록|수정")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "dsTypeCd")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MeasureDatasetUpsertDto.class, name = "MEASURE" ),
        @JsonSubTypes.Type(value = PipeNetworkDatasetUpsertDto.class, name = "PIPE_NETWORK" ),
        @JsonSubTypes.Type(value = UserDefinitionDatasetUpsertDto.class, name = "USER_DEF" )
})
@EqualsAndHashCode(callSuper = true)
public class DatasetUpsertDto extends GroupItemUpsertDto {
    @Schema(description = "데이터셋_ID")
    private String dsId;
    @Schema(description = "데이터셋명")
    private String dsNm;
    @Schema(description = "데이터셋설명")
    private String dsDesc;
    @Schema(description = "파일확장자")
    private FileExtension fileXtns;
}
