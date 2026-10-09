package com.hscmt.simulation.program.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.simulation.program.dto.ProgramResultUpsertDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pgm_rslt_m")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class ProgramResult extends DomainEventEntity {
    /* 프로그램결과_ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rslt_id")
    private String rsltId;
    /* 프로그램_ID */
    @Column(name = "pgm_id")
    private String pgmId;
    /* 프로그램결과명 */
    @Column(name = "rslt_nm")
    private String rsltNm;
    /* 파일확장자 */
    @Column(name = "file_xtns")
    @Enumerated(EnumType.STRING)
    private FileExtension fileXtns;

    public ProgramResult (ProgramResultUpsertDto dto) {
        this.pgmId = dto.getPgmId();
        this.rsltNm = dto.getRsltNm();
        this.fileXtns = dto.getFileXtns();
    }

    public void changeInfo (ProgramResultUpsertDto dto) {
        if (dto.getRsltNm() != null) {
            this.rsltNm = dto.getRsltNm();
        }
        if (dto.getFileXtns() != null) {
            this.fileXtns = dto.getFileXtns();
        }
    }
}
