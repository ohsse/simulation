package com.hscmt.waternet.tag.domain;

import com.hscmt.waternet.tag.domain.key.RwisDataKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class RwisData {
    /* tagsn, log_time */
    @EmbeddedId
    private RwisDataKey id;
    /* val */
    @Column(name = "val")
    private BigDecimal val;
}
