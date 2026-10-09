package com.hscmt.simulation.dataset.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hscmt.common.dto.BaseDto;
import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.util.QProjectionUtil;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetDto;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.hscmt.simulation.program.domain.QProgram;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.querydsl.core.types.Expression;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.group.GroupBy.list;

@Data
@NoArgsConstructor
@Schema(description = "데이터셋 정보")
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "dsTypeCd")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MeasureDatasetDto.class,     name = "MEASURE"),
        @JsonSubTypes.Type(value = PipeNetworkDatasetDto.class, name = "PIPE_NETWORK"),
        @JsonSubTypes.Type(value = UserDefinitionDatasetDto.class, name = "USER_DEF")
})
public class DatasetDto extends GroupItemDto {
    @Schema(description = "데이터셋_ID")
    private String dsId;
    @Schema(description = "데이터셋명")
    private String dsNm;
    @Schema(description = "데이터셋설명")
    private String dsDesc;
    @ArraySchema(schema = @Schema(implementation = ProgramDto.class, description = "프로그램정보"))
    private List<ProgramDto> programs;
    @Schema(description = "데이터셋유형", implementation = DatasetType.class)
    private DatasetType dsTypeCd;
    @Schema(description = "파일확장자", implementation = FileExtension.class)
    private FileExtension fileXtns;
    @ArraySchema(schema = @Schema(implementation = FileInfoDto.class, description = "파일정보"))
    private List<FileInfoDto> fileList = new ArrayList<>();

    public static List<Expression<?>> projectionFields (QDataset qDataset) {
        return QProjectionUtil.getCombinedExpressions(
                List.of(
                        qDataset.dsId,
                        qDataset.dsNm,
                        qDataset.dsDesc,
                        qDataset.sortOrd,
                        qDataset.grpId,
                        qDataset.fileXtns
                ),
                BaseDto.getBaseFields(qDataset)
        );
    }

    public static List<Expression<?>> projectionFields (QDataset qDataset, QProgram qProgram) {
        return QProjectionUtil.getCombinedExpressions(
                projectionFields(qDataset),
                List.of(
                        list(
                                QProjectionUtil.toQBean(ProgramDto.class, ProgramDto.projectionFields(qProgram))
                        ).as("programs")
                )
        );
    }
}

