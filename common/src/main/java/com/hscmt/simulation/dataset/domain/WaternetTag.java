package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.domain.BaseEntity;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/* 연계 태그정보 계측데이터에서 사용하는 워터넷태그 저장해두는 용도 */
@Entity
@Table(name = "wnet_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WaternetTag extends BaseEntity implements Persistable<String> {
    /* 태그번호 */
    @Id
    @Column(name = "tag_sn")
    private String tagSn;
    /* 태그유형코드 */
    @Column(name = "tag_se_cd")
    private String tagSeCd;
    /* 태그설명 */
    @Column(name = "tag_desc")
    private String tagDesc;
    /* 사용여부 : 사용여부 N -> 수집안함  */
    @Column(name = "use_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo useYn;

    public void changeTagDesc (String tagDesc) {
        this.tagDesc = tagDesc;
    }

    public WaternetTag (MeasureDatasetDetailUpsertDto dto) {
        this.tagSn = dto.getTagSn();
        this.tagSeCd = dto.getTagSeCd();
        this.tagDesc = dto.getTagDesc();
        this.useYn = YesOrNo.Y;
    }

    public void changeUseYn (YesOrNo useYn) {
        this.useYn = useYn;
    }

    @Override
    public String getId() {
        return this.tagSn;
    }

    @Override
    public boolean isNew() {
        return this.getRgstDttm() == null;
    }
}
