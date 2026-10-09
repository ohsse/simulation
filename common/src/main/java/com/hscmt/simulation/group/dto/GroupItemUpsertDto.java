package com.hscmt.simulation.group.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hscmt.simulation.dashboard.dto.DashboardUpsertDto;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.program.dto.ProgramUpsertDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "그룹항목")
@NoArgsConstructor
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "groupTypeCd")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DatasetUpsertDto.class, name = "DATASET" ),
        @JsonSubTypes.Type(value = ProgramUpsertDto.class, name = "PROGRAM" ),
        @JsonSubTypes.Type(value = DashboardUpsertDto.class, name = "DASHBOARD" ),
        @JsonSubTypes.Type(value = LayerUpsertDto.class, name = "LAYER")
})
public class GroupItemUpsertDto {
    @Schema(description = "그룹_ID")
    private String grpId;
    @Schema(description = "정렬순서")
    private Integer sortOrd;
}
