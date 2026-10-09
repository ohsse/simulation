package com.hscmt.simulation.program.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.program.dto.ProgramArgDto;
import com.hscmt.simulation.program.dto.ProgramUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pgm_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Program extends DomainEventEntity {
    /* 프로그램 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pgm_id")
    private String pgmId;
    /* 프로그램명 */
    @Column(name = "pgm_nm")
    private String pgmNm;
    /* 프로그램설명 */
    @Column(name = "pgm_desc")
    private String pgmDesc;
    /* 정렬순서 */
    @Column(name = "sort_ord")
    private Integer sortOrd;
    /* 그룹_ID */
    @Column(name = "grp_id")
    private String grpId;
    /* 가상환경_ID */
    @Column(name = "venv_id")
    private String venvId;
    /* 실시간여부 */
    @Column(name = "rltm_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo rltmYn;
    /* 실행반복주기유형 */
    @Column(name = "rptt_intv_type_cd")
    @Enumerated(EnumType.STRING)
    private CycleCd rpttIntvTypeCd;
    /* 실행반복주기간격 */
    @Column(name = "rptt_intv_val")
    private Integer rpttIntvVal;
    /* 최초실행일시 */
    @Column(name = "strt_exec_dttm")
    private LocalDateTime strtExecDttm;
    /* 프로그램실행인수목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pgm_args")
    private List<ProgramArgDto> pgmArgs = new ArrayList<>();
    /* 최종실행이력_ID */
    @Column(name = "fnl_exec_id")
    private String fnlExecId;
    /* 최종프로그램파일폴더ID */
    @Column(name = "fnl_pdir_id")
    private String fnlPdirId;

    public Program (ProgramUpsertDto dto) {
        this.pgmNm = dto.getPgmNm();
        this.pgmDesc = dto.getPgmDesc();
        this.sortOrd = dto.getSortOrd();
        this.grpId = dto.getGrpId();
        this.venvId = dto.getVenvId();
        this.rltmYn = dto.getRltmYn();
        this.rpttIntvTypeCd = dto.getRpttIntvTypeCd();
        this.rpttIntvVal = dto.getRpttIntvVal();
        this.strtExecDttm = dto.getStrtExecDttm();
        this.pgmArgs = dto.getPgmArgs();
    }

    public void changeInfo (ProgramUpsertDto dto) {
        if (dto.getPgmNm() != null) {
            this.pgmNm = dto.getPgmNm();
        }
        if (dto.getPgmDesc() != null) {
            this.pgmDesc = dto.getPgmDesc();
        }
        if (dto.getSortOrd() != null) {
            this.sortOrd = dto.getSortOrd();
        }
        if (dto.getGrpId() != null) {
            this.grpId = dto.getGrpId();
        }
        if (dto.getVenvId() != null) {
            this.venvId = dto.getVenvId();
        }
        if (dto.getRltmYn() != null) {
            this.rltmYn = dto.getRltmYn();
        }
        if (dto.getRpttIntvTypeCd() != null) {
            this.rpttIntvTypeCd = dto.getRpttIntvTypeCd();
        }
        if (dto.getRpttIntvVal() != null) {
            this.rpttIntvVal = dto.getRpttIntvVal();
        }
        if (dto.getStrtExecDttm() != null) {
            this.strtExecDttm = dto.getStrtExecDttm();
        }
        if (dto.getPgmArgs() != null) {
            this.pgmArgs = dto.getPgmArgs();
        }
    }

    public void changeFnlPdirId (String fnlPdirId) {this.fnlPdirId = fnlPdirId;}

    public void changeGrpInfo (String grpId, Integer sortOrd) {
        this.grpId = grpId;
        this.sortOrd = sortOrd;
    }
}