package com.hscmt.simulation.layer.domain;

import com.hscmt.common.domain.BaseEntity;
import com.hscmt.simulation.layer.key.LayerKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "layer_l")
@EqualsAndHashCode(callSuper = false)
public class LayerList extends BaseEntity {
    /* 레이어내역 키 */
    @EmbeddedId
    private LayerKey id;
    /* 좌표정보 */
    @Column(name = "gmtr_val")
    private Geometry gmtrVal;
    /* 속성정보 */
    @Column(name = "property")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> property = new LinkedHashMap<>();
    /* 색상문자열 */
    @Column(name = "color_str")
    private String colorStr;

}
