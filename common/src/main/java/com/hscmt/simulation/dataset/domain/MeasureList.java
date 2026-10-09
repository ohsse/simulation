package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.domain.BaseEntity;
import com.hscmt.simulation.dataset.key.MeasureListKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "msrm_l")
public class MeasureList extends BaseEntity {
    /* 계측내역 ID */
    @EmbeddedId
    private MeasureListKey id;
    /* 계측값 */
    @Column(name = "msrm_val")
    private BigDecimal msrmVal;
}
