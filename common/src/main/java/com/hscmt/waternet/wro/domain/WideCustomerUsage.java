package com.hscmt.waternet.wro.domain;

import com.hscmt.waternet.common.BaseEntity;
import com.hscmt.waternet.wro.domain.key.WideUsageCustomerKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "v_wd_cstmr_mrdng_l")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = true)
public class WideCustomerUsage extends BaseEntity {
    @EmbeddedId
    private WideUsageCustomerKey key;
    @Column(name = "cstmr_no")
    private String cstmrNo;

    @Column(name = "cstmr_nm")
    private String cstmrNm;

    @Column(name = "mt_usgqty")
    private Double mtUsgqty;

    @Column(name = "cntrct_stat_strt_de")
    private String cntrctStatStrtDe;

    @Column(name = "cntrct_stat_end_de")
    private String cntrctStatEndDe;

    @Column(name = "ori_cntrct_stat_strt_de")
    private String oriCntrctStatStrtDe;

    @Column(name = "ori_cntrct_stat_end_de")
    private String oriCntrctStatEndDe;
}
