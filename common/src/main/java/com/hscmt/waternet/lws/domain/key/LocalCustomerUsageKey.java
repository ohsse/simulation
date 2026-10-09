package com.hscmt.waternet.lws.domain.key;

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
public class LocalCustomerUsageKey {
    @Column(name = "stym")
    private String stym;
    @Column(name = "dmno")
    private String dmno;
}
