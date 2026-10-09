package com.hscmt.simulation.program.service;

import com.hscmt.common.enumeration.ExecutionType;
import com.hscmt.common.enumeration.InputFileType;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.exception.error.ProcessErrorCode;
import com.hscmt.common.response.CommandResult;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.ProcessUtil;
import com.hscmt.common.util.StringUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.program.domain.Program;
import com.hscmt.simulation.program.domain.ProgramExecHist;
import com.hscmt.simulation.program.dto.ProgramExecuteDto;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;
import com.hscmt.simulation.program.dto.ProgramResultDto;
import com.hscmt.simulation.program.dto.ProgramRunArgDto;
import com.hscmt.simulation.program.event.ProgramExecHistEventPublisher;
import com.hscmt.simulation.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class ProgramExecuteService {

    private final ProgramRepository programRepository;
    private final ProgramExecHistService programExecHistService;
    private final VirtualEnvironmentComponent vComp;
    private final ProgramExecHistEventPublisher publisher;
    private final String SPACE = " ";

    @SimulationTx
    public void terminateProgram (String histId) {
        programExecHistService.stopProgram(histId);
    }

    @SimulationTx
    public void executeProgram (ProgramExecuteDto programExecuteDto) {
        String pgmId = programExecuteDto.getPgmId();
        /* 프로그램 실행 전 프로그램 인풋 파일 세팅 */
        setUpProgramInputFiles(pgmId);
        /* 프로그램 커맨드 반영 */
        String command = getProgramCommand( programRepository.findProgramById(pgmId), programExecuteDto.getArgs());
        /* 프로그램 실행 */
        runProgram(pgmId, command, ExecutionType.MANUAL);
    }

    /* 프로그램 실행 기본은 스케줄 */
    @SimulationTx
    public void executeProgram(String pgmId) {
        /* 스케줄러 자동 실행 */
        executeProgram(pgmId, ExecutionType.SCHEDULED);
    }

    /* 프로그램 실행 */
    @SimulationTx
    public void executeProgram (String pgmId, ExecutionType executionType) {
        /* 프로그램 실행 전 프로그램 인풋 파일 세팅 */
        setUpProgramInputFiles(pgmId);
        /* 프로그램 실행을 위한 기본정보 조회 */
        String command = getProgramCommand( programRepository.findProgramById( pgmId ));
        /* 프로그램 실행 */
        runProgram(pgmId, command, executionType);
    }

    /* 프로그램 실행 */
    protected void runProgram (String pgmId, String command, ExecutionType executionType) {
        Process process;
        ProgramExecHist hist;

        try {
            process = ProcessUtil.getProcess(command);
            hist = programExecHistService.registerProgramExecHist(pgmId, executionType, String.valueOf(process.pid()));
        } catch (Exception e) {
            log.error("create process error : {}", e.getMessage());
            throw new RestApiException(ProcessErrorCode.CREATE_PROCESS_ERROR);
        }

        try {
            /* 프로세스 실행 결과 */
            CommandResult commandResult = ProcessUtil.runProcess(process);
            /* 실행 완료 */
            if (commandResult.getExitCode() == 0) {
                /* 성공 */
                publisher.successAndPublish(hist);
            } else {
                /* 오류 */
                programExecHistService.failProgram(hist.getHistId(), StringUtil.nvl(commandResult.getErrorMessage(), commandResult.getOutputMessage()));
            }

        } catch (Exception e) {
            /* 내부오류 */
            programExecHistService.failProgram(hist.getHistId(), e.getMessage());
        }
    }

    /* 실행에 필요한 인풋파일 세팅 */
    private void setUpProgramInputFiles (String pgmId) {
        /* 프로그램 실행 전에 디스크 용량 확보 */
        cleanupDiskFiles(pgmId);

        /* 프로그램 인풋파일 목록 가져오기 */
        List<ProgramInputFileDto> inputFiles = programRepository.findProgramInputFilesByPgmId(pgmId);

        String programDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId);

        List<File> targetFiles = new ArrayList<>();

        for (ProgramInputFileDto inputFile : inputFiles) {
            if (inputFile.getTrgtType() == InputFileType.DATASET) {
                String datasetDir = FileUtil.getDirPath(vComp.getDatasetBasePath(), inputFile.getTrgtId());

                if (new File(datasetDir).exists()) {
                    targetFiles.addAll(FileUtil.getOnlyFilesInDirectory(datasetDir));
                }

            } else {
                ProgramResultDto result = programRepository.findPgmIdByProgramResultId(inputFile.getTrgtId());
                for (File file : FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(vComp.getProgramBasePath(), result.getPgmId()))) {
                    result.getFileXtns().getValidExtensions().forEach(ext -> {
                        if (file.getName().endsWith(ext) && file.getName().equals(result.getRsltNm() + ext)) {
                            targetFiles.add(file);
                        }
                    });
                }
            }
        }

        for (File file : targetFiles) {
            try {
                FileUtil.copyFile(file.getAbsolutePath(), FileUtil.getFilePath(file.getName(), programDirPath));
            } catch (NoSuchFileException e) {
                log.error("copy file error : {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /* 프로그램 실행 커맨드 가져오기 */
    private String getProgramCommand (Program program, List<ProgramRunArgDto> args) {
        /* 실행할 가상환경 경로 */
        String venvPath = FileUtil.getDirPath(vComp.getVenvBasePath(), program.getVenvId());
        /* 프로그램 디렉토리 경로 */
        String programDirPath = FileUtil.getDirPath(vComp.getProgramBasePath(), program.getPgmId());

        StringBuffer command = new StringBuffer(FileUtil.getTargetRootDriveName(programDirPath))
                .append(" && ")
                .append("cd ")
                .append(programDirPath)
                .append(" && ")
                .append(vComp.getAnacondaActivatePath())
                .append(SPACE)
                .append(venvPath)
                .append(" && ")
                .append("python ")
                ;
        File pythonScript = FileUtil.getOnlyFilesInDirectory(FileUtil.getDirPath(programDirPath, vComp.REVISION_DIR, program.getFnlPdirId()))
                .stream().findFirst().orElse(null);
        String pythonFileName = pythonScript != null ? pythonScript.getName() : null;
        command.append(pythonFileName);

        /* 실행 인수 존재시 실행인수 반영 */
        if (args != null && !args.isEmpty()) {
            for (ProgramRunArgDto arg : args) {
                command.append(SPACE);
                command.append(arg.getName() + SPACE + arg.getValue());
            }    
        }

        return command.toString();
    }

    /* 프로그램 실행 커맨드 가져오기 */
    private String getProgramCommand (Program program) {
        return getProgramCommand(program, new ArrayList<>());
    }

    /* 프로그램 실행 전에 디스크 용량 확인하고 용량 부족시 과거 이력 삭제 */
    private void cleanupDiskFiles (String pgmId) {
        List<ProgramExecHist> list = programExecHistService.findAllByPgmIdForCheck(pgmId);

        if (list.isEmpty()) return;

        Long totalBytes = list.stream().mapToLong(x -> x.getRsltBytes()).sum();
        Long usableBytes = getUnallocatedDiskSize();

        /* 2개의 용량이 남은 용량보다 크다면 */
        if (usableBytes < totalBytes) {
            log.error("disk space is not enough : usableBytes = {}, totalBytes = {}", usableBytes, totalBytes);
            publisher.deleteAllAndPublish(list);
        }
    }

    /* 드라이브 용량 가져오기 */
    public long getUnallocatedDiskSize () {
        Path root = Paths.get(vComp.getFileServerBasePath()).getRoot();
        try {
            FileStore store = Files.getFileStore(root);
            return store.getUnallocatedSpace();
        } catch (Exception e) {
            throw new RestApiException(FileErrorCode.FILE_EXPLORER_ERROR);
        }
    }
}
