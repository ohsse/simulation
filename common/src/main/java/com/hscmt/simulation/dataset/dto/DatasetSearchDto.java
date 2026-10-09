package com.hscmt.simulation.dataset.dto;

import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.simulation.dataset.domain.MeasureDataset;
import com.hscmt.simulation.dataset.domain.PipeNetworkDataset;
import com.hscmt.simulation.dataset.domain.QDataset;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description = "데이터셋 검색 요청")
public class DatasetSearchDto {
    @Schema(description = "데이터셋 유형 코드", implementation = DatasetType.class)
    private DatasetType dsTypeCd;
    @Schema(description = "검색어")
    private String searchWord;
    @Schema(description = "그룹_ID")
    private String grpId;

    public static BooleanBuilder getConditionByDto (QDataset qDataset, DatasetSearchDto dto) {
        BooleanBuilder builder = new BooleanBuilder();

        if (dto.getDsTypeCd() != null) {
            switch (dto.getDsTypeCd()) {
                case MEASURE -> builder.and(Expressions.booleanTemplate("type({0}) = {1}", qDataset, MeasureDataset.class));
                case PIPE_NETWORK -> builder.and(Expressions.booleanTemplate("type({0}) = {1}", qDataset, PipeNetworkDataset.class));
                case USER_DEF -> builder.and(Expressions.booleanTemplate("type({0}) = {1}", qDataset, com.hscmt.simulation.dataset.domain.UserDefinitionDataset.class));
            }
        }

        if (dto.getGrpId() != null && !dto.getGrpId().isEmpty()) {
            builder.and(qDataset.grpId.eq(dto.getGrpId()));
        }

        if (dto.getSearchWord() != null && !dto.getSearchWord().isEmpty()) {
            builder.and(
                    qDataset.dsNm.like("%" + dto.getSearchWord() + "%")
                            .or(qDataset.dsDesc.like("%" + dto.getSearchWord() + "%"))
            );
        }

        return builder;
    }
}
