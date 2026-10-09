package com.hscmt.simulation.layer.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.common.enumeration.FeatureType;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.layer.dto.LayerStyleInfo;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.program.domain.ProgramResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "layer_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(callSuper = false)
public class Layer extends DomainEventEntity {
    /* 레이어ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "layer_id")
    private String layerId;
    /* 레이어명 */
    @Column(name = "layer_nm")
    private String layerNm;
    /* 레이어설명 */
    @Column(name = "layer_desc")
    private String layerDesc;
    /* 초기표출여부 */
    @Column(name = "init_dspy_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo initDspyYn;
    /* 좌표계 */
    @Column(name = "crsy_type_cd")
    @Enumerated(EnumType.STRING)
    private CrsyType crsyTypeCd;
    /* 레이어스타일 */
    @Column(name = "layer_styles")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<LayerStyleInfo> layerStyles = new ArrayList<>();
    /* 사용가능여부 */
    @Column(name = "use_able_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo useAbleYn;
    /* 레이어객체유형 */
    @Column(name = "layer_ftype")
    @Enumerated(EnumType.STRING)
    private FeatureType layerFtype;
    /* 그룹ID */
    @Column(name = "grp_id")
    private String grpId;
    /* 정렬순서 */
    @Column(name = "sort_ord")
    private Integer sortOrd;
    @Column(name = "properties")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> properties = new ArrayList<>();

    public void changeUseAbleYn (YesOrNo useAbleYn) {
        this.useAbleYn = useAbleYn;
    }

    public Layer (LayerUpsertDto dto) {
        if (dto.getLayerId() != null && !dto.getLayerId().isEmpty()) {
            this.layerId = dto.getLayerId();
        }
        this.layerNm = dto.getLayerNm();
        this.layerDesc = dto.getLayerDesc();
        this.initDspyYn = dto.getInitDspyYn();
        this.crsyTypeCd = dto.getCrsyTypeCd();
        this.layerStyles = dto.getLayerStyles();
        this.useAbleYn = YesOrNo.N;
    }

    public void changeInfo (LayerUpsertDto dto) {
        if (dto.getLayerNm() != null) {
            this.layerNm = dto.getLayerNm();
        }
        if (dto.getLayerDesc() != null) {
            this.layerDesc = dto.getLayerDesc();
        }
        if (dto.getInitDspyYn() != null) {
            this.initDspyYn = dto.getInitDspyYn();
        }
        if (dto.getCrsyTypeCd() != null) {
            this.crsyTypeCd = dto.getCrsyTypeCd();
        }
        if (dto.getLayerStyles() != null) {
            this.layerStyles = dto.getLayerStyles();
        }
    }

    public static Layer fromShpResult (ProgramResult result) {
        LayerUpsertDto dto = new LayerUpsertDto();
        dto.setLayerId(result.getRsltId());
        dto.setLayerNm(result.getRsltNm());
        dto.setInitDspyYn(YesOrNo.N);
        dto.setCrsyTypeCd(CrsyType.EPSG5186);

        Layer newLayer = new Layer(dto);
        newLayer.setRgstId(result.getRgstId());
        newLayer.setRgstDttm(result.getRgstDttm());
        newLayer.setMdfId(result.getMdfId());
        newLayer.setMdfDttm(result.getMdfDttm());

        return newLayer;
    }

    public void changeGrpInfo (String grpId, Integer sortOrd) {
        this.grpId = grpId;
        this.sortOrd = sortOrd;
    }
}
