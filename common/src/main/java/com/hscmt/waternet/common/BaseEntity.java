package com.hscmt.waternet.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public class BaseEntity {
    @Column(name = "distr_cd")
    private String distrCd;
    @Column(name = "distr_nm")
    private String distrNm;
    @Column(name = "mgc_cd")
    private String mgcCd;
    @Column(name = "mgc_nm")
    private String mgcNm;
}
