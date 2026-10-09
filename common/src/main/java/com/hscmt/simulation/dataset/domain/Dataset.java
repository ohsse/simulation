package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ds_m")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ds_type_cd", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Dataset extends DomainEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ds_id")
    private String dsId;

    @Column(name = "ds_nm")
    private String dsNm;

    @Column(name = "ds_desc")
    private String dsDesc;

    @Column(name = "sort_ord")
    private Integer sortOrd;

    @Column(name = "grp_id")
    private String grpId;

    @Column(name = "file_xtns")
    @Enumerated(EnumType.STRING)
    private FileExtension fileXtns;

    /* 생성 */
    public Dataset (DatasetUpsertDto dto) {
        this.dsNm = dto.getDsNm();
        this.dsDesc = dto.getDsDesc();
        this.sortOrd = dto.getSortOrd();
        this.grpId = dto.getGrpId();
        this.fileXtns = dto.getFileXtns();
    }

    /* 정보수정 */
    public void changeInfo (DatasetUpsertDto dto) {
        if (dto.getDsNm() != null) {
            this.dsNm = dto.getDsNm().isEmpty() ? null : dto.getDsNm();
        }
        if (dto.getDsDesc() != null) {
            this.dsDesc = dto.getDsDesc().isEmpty() ? null : dto.getDsDesc();
        }
        if (dto.getSortOrd() != null) {
            this.sortOrd = dto.getSortOrd();
        }
        if (dto.getFileXtns() != null) {
            this.fileXtns = dto.getFileXtns();
        }
        if (dto.getGrpId() != null) {
            this.grpId = dto.getGrpId().isEmpty() ? null : dto.getGrpId();
        }

        if (dto instanceof MeasureDatasetUpsertDto md) {
            MeasureDataset mdTarget = (MeasureDataset) this;
            mdTarget.changeInfo(md);
        }

        if (dto instanceof PipeNetworkDatasetUpsertDto pd) {
            PipeNetworkDataset pdTarget = (PipeNetworkDataset) this;
            pdTarget.changeInfo(pd);
        }
    }

    public void changeGrpInfo (String grpId, Integer sortOrd) {
        this.grpId = grpId;
        this.sortOrd = sortOrd;
    }
}
