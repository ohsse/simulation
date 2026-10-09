package com.hscmt.simulation.program.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.simulation.program.dto.ProgramVisualizationUpsertDto;
import com.hscmt.simulation.program.dto.vis.ImageSetupItemDto;
import com.hscmt.simulation.program.dto.vis.VisSetupItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pgm_vis_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProgramVisualization extends DomainEventEntity {
    /* 프로그램시각화_ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vis_id")
    private String visId;
    /* 프로그램_ID */
    @Column(name = "pgm_id")
    private String pgmId;
    /* 시각화이름 */
    @Column(name = "vis_nm")
    private String visNm;
    /* 시각화유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "vis_type_cd")
    private VisTypeCd visTypeCd;
    /* 시각화설정 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vis_setup_text")
    private VisSetupItem visSetupText;
//    private Map<String, Object> visSetupText;

    public ProgramVisualization (ProgramVisualizationUpsertDto dto) {
        if (dto.getVisId() != null && !dto.getVisId().isBlank()) {
            this.visId = dto.getVisId();
        }
        this.pgmId = dto.getPgmId();
        this.visNm = dto.getVisNm();
        this.visTypeCd = dto.getVisTypeCd();
        this.visSetupText = dto.getVisSetupText();
    }

    public void changeInfo (ProgramVisualizationUpsertDto dto) {
        if (dto.getVisNm() != null) {
            this.visNm = dto.getVisNm();
        }
        if (dto.getVisTypeCd() != null) {
            this.visTypeCd = dto.getVisTypeCd();
        }
        if (dto.getVisSetupText() != null) {
            this.visSetupText = dto.getVisSetupText();
        }
    }

    public static ProgramVisualization fromImageResult (ProgramResult result) {
        ProgramVisualizationUpsertDto dto = new ProgramVisualizationUpsertDto();
        dto.setPgmId(result.getPgmId());
        dto.setVisNm(result.getRsltNm());
        dto.setVisTypeCd(VisTypeCd.IMAGE);
        dto.setVisId(result.getRsltId());

        String orgFileName = result.getRsltNm() + result.getFileXtns().getValidExtensions().getFirst();
        String pgmRsltNm = result.getRsltNm();

        ImageSetupItemDto imageSetupItemDto = new ImageSetupItemDto();
        imageSetupItemDto.setPgmRsltNm(pgmRsltNm);
        imageSetupItemDto.setFileName(orgFileName);
        dto.setVisSetupText(imageSetupItemDto);

        ProgramVisualization vis = new ProgramVisualization(dto);
        vis.setRgstId(result.getRgstId());
        vis.setRgstDttm(result.getRgstDttm());
        vis.setMdfId(result.getMdfId());
        vis.setMdfDttm(result.getMdfDttm());
        return vis;
    }
}
