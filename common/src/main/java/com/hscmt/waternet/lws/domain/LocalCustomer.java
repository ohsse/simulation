package com.hscmt.waternet.lws.domain;

import com.hscmt.waternet.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_CSTMR_L")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LocalCustomer extends BaseEntity {
    @Id
    @Column(name= "dmno")
    private String dmno;

    @Column(name = "sgccd")
    private String sgccd;

    @Column(name = "lfclty_nm")
    private String lfcltyNm;

    @Column(name = "mfclty_nm")
    private String mfcltyNm;

    @Column(name = "sfclty_nm")
    private String sfcltyNm;

    @Column(name = "dmnm")
    private String dmnm;

    @Column(name = "dmaddr")
    private String dmaddr;
}
