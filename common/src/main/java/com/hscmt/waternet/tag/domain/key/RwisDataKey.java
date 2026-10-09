package com.hscmt.waternet.tag.domain.key;

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
public class RwisDataKey {
    @Column(name = "log_time")
    private String logTime;
    @Column(name = "tagsn")
    private Long tagsn;
}
