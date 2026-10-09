package com.hscmt.waternet.lws.domain;

import com.hscmt.waternet.common.BaseEntity;
import com.hscmt.waternet.lws.domain.key.LocalCustomerUsageKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "v_cstmr_mrdng_l")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LocalCustomerUsage{
    @EmbeddedId
    private LocalCustomerUsageKey key;

    @Column(name = "ori_stym")
    private String oriStym;

    @Column(name = "wsusevol")
    private Double wsusevol;

    @Column(name = "wsstvol")
    private Double wsstvol;
}
