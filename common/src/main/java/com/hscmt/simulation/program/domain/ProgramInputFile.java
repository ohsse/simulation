package com.hscmt.simulation.program.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.InputFileType;
import com.hscmt.simulation.program.dto.ProgramInputFileUpsertDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pgm_input_file_m")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
public class ProgramInputFile extends DomainEventEntity {
    /* 프로그램입력파일_ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "input_file_id")
    private String inputFileId;
    /* 프로그램_ID */
    @Column(name = "pgm_id")
    private String pgmId;
    /* 대상ID : 프로그램결과_ID or 데이터셋_ID */
    @Column(name = "trgt_id")
    private String trgtId;
    /* 대상유형코드 */
    @Enumerated(EnumType.STRING)
    @Column(name = "trgtTypeCd")
    private InputFileType trgtType;

    public ProgramInputFile (ProgramInputFileUpsertDto dto) {
        this.pgmId = dto.getPgmId();
        this.trgtId = dto.getTrgtId();
        this.trgtType = dto.getTrgtType();
    }
}
