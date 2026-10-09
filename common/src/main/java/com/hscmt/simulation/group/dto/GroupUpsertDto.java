package com.hscmt.simulation.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "그룹 추가|수정")
@NoArgsConstructor
@Data
public class GroupUpsertDto {
    @Schema(description = "그룹_ID")
    private String grpId;
    @Schema(description = "그룹명")
    private String grpNm;
    @Schema(description = "그룹설명")
    private String grpDesc;
    @Schema(description = "정렬순서")
    private Integer sortOrd;
    @Schema(description = "상위그룹_ID")
    private String upGrpId;
    @Schema(description = "하위그룹목록")
    private List<GroupUpsertDto> children = new ArrayList<>();
    @Schema(description = "그룹아이템목록")
    private List<GroupItemUpsertDto> items = new ArrayList<>();


    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }
}
