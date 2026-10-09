package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "msrm_ds_d")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MeasureDatasetDetail extends DomainEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ds_itm_id")
    private String dsItmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ds_id")
    private Dataset dataset;

    @Column(name = "tag_sn")
    private String tagSn;

    @Column(name = "sort_ord")
    private Integer sortOrd;

    public MeasureDatasetDetail (Dataset dataset, MeasureDatasetDetailUpsertDto dto) {
        this.dataset = dataset;
        this.tagSn = dto.getTagSn();
        this.sortOrd = dto.getSortOrd();
    }
}
