package com.hscmt.simulation.dataset.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class MeasureListKey {
    @Column(name = "tag_sn")
    private String tagSn;
    @Column(name = "msrm_dttm")
    private LocalDateTime msrmDttm;
}
