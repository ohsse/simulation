package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "msrm_ds_m")
@DiscriminatorValue("MEASURE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MeasureDataset extends Dataset {
    /* 실시간여부 */
    @Column(name = "rltm_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo rltmYn;
    /* 시작일시 */
    @Column(name = "strt_dttm")
    private LocalDateTime strtDttm;
    /* 종료일시 */
    @Column(name = "end_dttm")
    private LocalDateTime endDttm;
    /* 생성주기 */
    @Column(name = "term_type_cd")
    @Enumerated(EnumType.STRING)
    private CycleCd termTypeCd;
    /* 조회기간 */
    @Column(name = "inqy_term")
    private Integer inqyTerm;


    /* 데이터셋항목목록 */
    @OneToMany(mappedBy = "dataset", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<MeasureDatasetDetail> detailItems = new ArrayList<>();

    public MeasureDataset (MeasureDatasetUpsertDto dto) {
        super(dto);
        this.rltmYn = dto.getRltmYn();
        this.strtDttm = dto.getStrtDttm();
        this.endDttm = dto.getEndDttm();
        this.termTypeCd = dto.getTermTypeCd();
        this.inqyTerm = dto.getInqyTerm();
    }

    public void changeInfo (MeasureDatasetUpsertDto dto) {
        if (dto.getRltmYn() != null) {
            this.rltmYn = dto.getRltmYn();
        }
        if (dto.getStrtDttm() != null) {
            this.strtDttm = dto.getStrtDttm();
        }
        if (dto.getEndDttm() != null) {
            this.endDttm = dto.getEndDttm();
        }
        if (dto.getInqyTerm() != null) {
            this.termTypeCd = dto.getTermTypeCd();
        }
        if (dto.getTermTypeCd() != null) {
            this.inqyTerm = dto.getInqyTerm();
        }
    }

}
