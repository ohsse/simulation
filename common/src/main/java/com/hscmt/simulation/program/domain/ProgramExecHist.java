package com.hscmt.simulation.program.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Entity
@Table(name = "pgm_exec_h")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProgramExecHist extends DomainEventEntity {
    /* 이력_ID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hist_id")
    private String histId;
    /* 프로그램_ID */
    @Column(name = "pgm_id")
    private String pgmId;
    /* 프로그램실행시작시간 */
    @Column(name = "exec_strt_dttm")
    private LocalDateTime execStrtDttm;
    /* 프로그램실행종료시간 */
    @Column(name = "exec_end_dttm")
    private LocalDateTime execEndDttm;
    /* 실행유형코드 */
    @Column(name = "exec_type_cd")
    @Enumerated(EnumType.STRING)
    private ExecutionType execTypeCd;
    /* 실행상태코드 */
    @Column(name = "exec_stts_cd")
    @Enumerated(EnumType.STRING)
    private ExecStat execSttsCd;
    /* 프로세스_ID */
    @Column(name = "procs_id")
    private String procsId;
    /* 결과폴더ID */
    @Column(name = "rslt_dir_id")
    private String rsltDirId;
    /* 에러내용 */
    @Column(name = "err_text", columnDefinition = "text")
    private String errText;
    /* 결과파일용량 */
    @Column(name = "rslt_bytes")
    private Long rsltBytes;

    public ProgramExecHist (String pgmId, ExecutionType execTypeCd, String procsId) {
        this.pgmId = pgmId;
        this.execTypeCd = execTypeCd;
        this.execStrtDttm = LocalDateTime.now();
        this.procsId = procsId;
        this.execSttsCd = ExecStat.RUNNING;
    }

    public void changeRsltDirId (String rsltDirId) {this.rsltDirId = rsltDirId;}
    public void changeRsltBytes (Long rsltBytes) {this.rsltBytes = rsltBytes;}

    public void success() {
        this.execEndDttm = LocalDateTime.now();
        this.execSttsCd = ExecStat.COMPLETED;
        this.procsId = null;
    }

    public void fail (String message) {
        this.execEndDttm = LocalDateTime.now();
        this.execSttsCd = ExecStat.ERROR;
        this.errText = message;
        this.procsId = null;
    }

    public void stop () {
        this.execEndDttm = LocalDateTime.now();
        this.execSttsCd = ExecStat.TERMINATED;
        this.procsId = null;
    }


}
