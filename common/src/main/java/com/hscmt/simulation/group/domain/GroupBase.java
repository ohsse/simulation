package com.hscmt.simulation.group.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class GroupBase extends DomainEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grp_id")
    private String grpId;

    @Column(name = "grp_nm")
    protected String grpNm;

    @Column(name = "grp_desc")
    protected String grpDesc;

    @Column(name = "sort_ord")
    protected Integer sortOrd;

    @Column(name = "up_grp_id")
    protected String upGrpId;

    protected GroupBase(GroupUpsertDto dto) {
        this.grpNm = dto.getGrpNm();
        this.sortOrd = dto.getSortOrd();
        this.grpDesc = dto.getGrpDesc();
        this.upGrpId = dto.getUpGrpId();
    }

    public void changeInfo (GroupUpsertDto dto) {
        if (dto.getGrpNm() != null) {
            this.grpNm = dto.getGrpNm();
        }
        if (dto.getSortOrd() != null) {
            this.sortOrd = dto.getSortOrd();
        }
        if (dto.getGrpDesc() != null) {
            this.grpDesc = dto.getGrpDesc();
        }
    }

    public void changeParent (GroupBase parent) {
        this.upGrpId = parent.getGrpId();
    }

    public void setParentId (String parentId) {
        this.upGrpId = parentId;
    }
}
