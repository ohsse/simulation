package com.hscmt.simulation.group.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.enumeration.GroupType;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dashboard.domain.QDashboard;
import com.hscmt.simulation.dashboard.dto.DashboardDto;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.layer.domain.QLayer;
import com.hscmt.simulation.layer.dto.LayerDto;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;


@Schema(description = "그룹항목")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "groupTypeCd")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DatasetDto.class, name = "DATASET" ),
        @JsonSubTypes.Type(value = ProgramDto.class, name = "PROGRAM" ),
        @JsonSubTypes.Type(value = DashboardDto.class, name = "DASHBOARD" ),
        @JsonSubTypes.Type(value = LayerDto.class, name = "LAYER")
})
public class GroupItemDto extends BaseDto {
    @Schema(description = "그룹_ID")
    private String grpId;
    @Schema(description = "정렬순서")
    private Integer sortOrd;


    public static List<Expression<?>> projectionFields (GroupType groupType) {
        return switch (groupType){
            case DATASET -> DatasetDto.projectionFields(QDataset.dataset);
            case PROGRAM -> ProgramDto.projectionFields(QProgram.program);
            case DASHBOARD -> DashboardDto.projectionFields(QDashboard.dashboard);
            case LAYER -> LayerDto.projectionFields(QLayer.layer);
            default -> null;
        };
    }

    public static QBean<?> toQBean(GroupType groupType){
        return switch (groupType){
            case DATASET -> QProjectionUtil.toQBean(DatasetDto.class, projectionFields(groupType));
            case PROGRAM -> QProjectionUtil.toQBean(ProgramDto.class, projectionFields(groupType));
            case DASHBOARD -> QProjectionUtil.toQBean(DashboardDto.class, projectionFields(groupType));
            case LAYER -> QProjectionUtil.toQBean(LayerDto.class, projectionFields(groupType));
            default -> null;
        };
    }
}
