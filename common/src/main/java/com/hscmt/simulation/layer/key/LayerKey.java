package com.hscmt.simulation.layer.key;

import com.hscmt.common.enumeration.FeatureType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class LayerKey implements Serializable {
    @Column(name = "layer_id")
    private String layerId;
    @Column(name = "ftype")
    private FeatureType ftype;
    @Column(name = "fid")
    private Long fid;

}
