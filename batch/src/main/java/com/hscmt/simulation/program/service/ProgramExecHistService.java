package com.hscmt.simulation.program.service;

import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.response.CommandResult;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.ProcessUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.program.domain.ProgramExecHist;
import com.hscmt.simulation.program.event.ProgramExecHistEventPublisher;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class ProgramExecHistService {
    private final VirtualEnvironmentComponent vComp;
    private final ProgramExecHistRepository repository;
    private final Long LIMIT_COUNT = 5L;
    private final ProgramExecHistEventPublisher publisher;


    @SimulationTx
    public void deleteByHistId (String histId) {
        repository.findById(histId)
                .ifPresent(x -> publisher.deleteAndPublish(x));
    }

    /* 실행이력 생성 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public ProgramExecHist registerProgramExecHist (String pgmId, ExecutionType executionType, String procsId) {
        return repository.save(new ProgramExecHist(pgmId, executionType , procsId));
    }

    /* 성공이력 저장 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void successProgram (String histId) {
        repository.findById(histId)
                .ifPresent( findHist -> {
                    findHist.success();
                });
    }

    /* 실패이력 저장 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void failProgram (String histId, String message) {
        repository.findById(histId)
                .ifPresent( findHist -> {
                    if (findHist.getExecSttsCd() == ExecStat.TERMINATED) return;
                    fail(findHist, message);
                });
    }

    /* 중지이력 저장 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void stopProgram (String histId) {
        repository.findById(histId)
                .ifPresent( findHist -> {
                    try {
                        CommandResult result = ProcessUtil.killProcess(findHist.getProcsId());

                        log.error("kill process result : {}", result.getExitCode());
                        log.error("kill process message : {}", result.getErrorMessage());
                        log.error("processId : {}", findHist.getProcsId() );

                        if (result.getExitCode() != 0) {
                            log.error("kill process error : {}", result.getErrorMessage());
                            fail(findHist, result.getErrorMessage());
                        } else {
                            log.error("kill process success");
                            findHist.stop();
                        }
                    } catch (Exception e) {
                        log.error("kill process error in java : {}", e.getMessage());
                        fail(findHist, e.getMessage());
                    }
                });
    }

    /* 결과 폴더 저장 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void changeResultDir (String histId, String resultDir) {
        repository.findById(histId)
                .ifPresent(findHist -> {
                    findHist.changeRsltDirId(resultDir);
                    findHist.changeRsltBytes(FileUtil.getDirectorySize(Paths.get(FileUtil.getDirPath(vComp.getProgramBasePath(), findHist.getPgmId(), vComp.EXEC_RESULT_DIR, resultDir))));
                });
    }

    /* 실패다 실패 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    protected void fail (ProgramExecHist hist, String message) {
        log.error("fail program : {}", message);
        hist.fail(message);
    }

    public List<ProgramExecHist> findAllByPgmIdForCheck (String pgmId) {
        return repository.findByPgmIdLimit(pgmId, LIMIT_COUNT);
    }
}
