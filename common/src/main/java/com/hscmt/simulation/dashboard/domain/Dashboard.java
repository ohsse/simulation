package com.hscmt.simulation.dashboard.domain;

import com.hscmt.common.domain.BaseEntity;
import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.simulation.dashboard.dto.DashboardUpsertDto;
import com.hscmt.simulation.dashboard.dto.DsbdVisItemDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dsbd_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Dashboard extends BaseEntity {
    /* 대시보드_ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dsbd_id")
    private String dsbdId;
    /* 대시보드명 */
    @Column(name = "dsbd_nm")
    private String dsbdNm;
    /* 대시보드설명 */
    @Column(name = "dsbd_desc")
    private String dsbdDesc;
    /* 해상도가로값 */
    @Column(name = "res_width_val")
    private Integer resWidthVal;
    /* 해상도세로값 */
    @Column(name = "res_hgln_val")
    private Integer resHglnVal;
    /* 그룹_ID */
    @Column(name = "grp_id")
    private String grpId;
    /* 정렬순서 */
    @Column(name = "sort_ord")
    private Integer sortOrd;
    /* 대시보드시각화목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dsbd_vis_items")
    private List<DsbdVisItemDto> items = new ArrayList<>();

    public void changeItems (List<DsbdVisItemDto> items) {
        this.items = items;
    }

    public Dashboard (DashboardUpsertDto dto) {
        this.dsbdNm = dto.getDsbdNm();
        this.dsbdDesc = dto.getDsbdDesc();
        this.resWidthVal = dto.getResWidthVal();
        this.resHglnVal = dto.getResHglnVal();
        this.grpId = dto.getGrpId();
        this.sortOrd = dto.getSortOrd();
        this.items = dto.getItems();
    }

    public void changeInfo (DashboardUpsertDto dto) {
        if (dto.getDsbdNm() != null) {
            this.dsbdNm = dto.getDsbdNm();
        }
        if (dto.getDsbdDesc() != null) {
            this.dsbdDesc = dto.getDsbdDesc();
        }
        if (dto.getResWidthVal() != null) {
            this.resWidthVal = dto.getResWidthVal();
        }
        if (dto.getResHglnVal() != null) {
            this.resHglnVal = dto.getResHglnVal();
        }
        if (dto.getGrpId() != null) {
            this.grpId = dto.getGrpId();
        }
        if (dto.getSortOrd() != null) {
            this.sortOrd = dto.getSortOrd();
        }
        if (dto.getItems() != null) {
            this.items = dto.getItems();
        }
    }

    public void changeGrpInfo (String grpId, Integer sortOrd) {
        this.grpId = grpId;
        this.sortOrd = sortOrd;
    }
}
