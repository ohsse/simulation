package com.hscmt.waternet.wro.domain.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WideUsageCustomerKey {
    @Column(name = "use_ym")
    private String useYm;
    @Column(name = "mrnr_no")
    private String mrnrNo;
    @Column(name = "fclty_mrnr_no")
    private String fcltyMrnrNo;
}
