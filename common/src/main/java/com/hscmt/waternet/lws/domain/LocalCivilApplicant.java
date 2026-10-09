package com.hscmt.waternet.lws.domain;

import com.hscmt.waternet.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "v_cstmr_cainfo")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LocalCivilApplicant extends BaseEntity {
    @Id
    @Column(name = "cano")
    private String cano;

    @Column(name = "calrgcd")
    private String calrgcd;

    @Column(name = "camidcd")
    private String camidcd;

    @Column(name = "caappldt")
    private String caappldt;

    @Column(name = "supdt")
    private String supdt;

    @Column(name = "calrgnm")
    private String calrgnm;

    @Column(name = "camidnm")
    private String camidnm;

    @Column(name = "caprcsrslt")
    private String caprcsrslt;

    @Column(name = "prcsdt")
    private String prcsdt;
}
