package com.hscmt.simulation.program.service;

import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.DownloadType;
import com.hscmt.common.enumeration.ExecStat;
import com.hscmt.common.util.DateTimeUtil;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.StringUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.hscmt.simulation.program.dto.ProgramExecSearchDto;
import com.hscmt.simulation.program.dto.ProgramFileDto;
import com.hscmt.simulation.program.repository.ProgramExecHistRepository;
import com.hscmt.simulation.program.repository.ProgramRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class ProgramExecHistService {

    private final ProgramRepository programRepository;
    private final ProgramExecHistRepository programExecHistRepository;
    private final VirtualEnvironmentComponent vComp;

    public void downloadFiles (DownloadType type, String pgmId, List<String> dirIdList, HttpServletResponse response) {
        ProgramDto programDto = programRepository.findProgramById(pgmId);
        
        String fileName = switch (type) {
            case RESULT -> programDto.getPgmNm() + "_실행결과.zip";
            case REVISION -> programDto.getPgmNm() + "_개정이력.zip";
        };

        String cd = StringUtil.contentDispositionFileName(fileName);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, cd);
        response.setContentType(String.valueOf(MediaType.parseMediaType("application/zip")));

        String resultDirBasePath = switch(type) {
            case RESULT -> FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.EXEC_RESULT_DIR);
            case REVISION -> FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.REVISION_DIR);
        };

        try (
                OutputStream os = response.getOutputStream();
                ZipOutputStream zos = new ZipOutputStream(os);
        ) {
            for (String dirId : dirIdList) {
                File resultFolder = new File (FileUtil.getDirPath(resultDirBasePath, dirId));

                if (resultFolder.exists()) {
                    FileUtil.mergeToZip(resultFolder.toPath(), dirId, zos);
                }
            }
        } catch (Exception e) {
            log.error("zip download error : {} ", e.getMessage());
            throw new RuntimeException(e);
        }
    }




    public List<ProgramFileDto> findAllProgramFileHist (String pgmId) {
        File programFileHistDir = new File(FileUtil.getDirPath(vComp.getProgramBasePath(), pgmId, vComp.REVISION_DIR));

        File[] files = programFileHistDir.listFiles();

        List<ProgramFileDto> result = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                String programDirPath = file.getAbsolutePath();
                List<File> programFiles = FileUtil.getOnlyFilesInDirectory(programDirPath);

                for (File programFile : programFiles) {
                    ProgramFileDto dto = new ProgramFileDto();
                    dto.setRgstDttm(DateTimeUtil.convertUuidStringToLocalDateTime(file.getName()));
                    dto.setFileNm(FileUtil.getFileNameWithoutExtension(programFile.getName()));
                    dto.setFileExtension(FileUtil.getFileExtension(programFile.getName()));
                    dto.setFileUrl(FileUtil.getUrlPath(programFile.getName(), vComp.getProgramBasePath().replace(vComp.getFileServerBasePath(),""), pgmId, vComp.REVISION_DIR, file.getName()));
                    dto.setFullFileName(programFile.getName());
                    dto.setFileDirId(programFile.getParent());
                    result.add(dto);
                }

            }
        }

        result.sort(Comparator.comparing(ProgramFileDto::getRgstDttm).reversed());

        return result;
    }


    public List<ProgramExecHistDto> findAllProgramExecHistList (ProgramExecSearchDto searchDto){
        List<ProgramExecHistDto> histList = programExecHistRepository.findAllProgramExecHistList(searchDto);
        if (searchDto.getPgmId() != null) {
            setResultFiles (histList);
        }
        return histList;
    }

    private void setResultFiles (List<ProgramExecHistDto> list) {
        for (ProgramExecHistDto dto : list) {

            if (dto.getRsltDirId() != null && dto.getExecSttsCd() == ExecStat.COMPLETED) {
                setResultFiles(dto);
            }

        }
    }

    private void setResultFiles (ProgramExecHistDto dto) {
        /* 결과파일 목록 세팅 */
        List<FileInfoDto> resultFiles = dto.getResultFiles();
        /* 프로그램 결과파일 베이스 경로 */
        String rsltBaseDir = FileUtil.getDirPath(vComp.getProgramBasePath(), dto.getPgmId(), vComp.getEXEC_RESULT_DIR());
        /* 해당실행이력의 결과파일 경로 */
        String targetDirPath = FileUtil.getDirPath(rsltBaseDir, dto.getRsltDirId());

        /* 결과파일 경로 내 파일정보 세팅 */
        FileUtil.getOnlyFilesInDirectory(targetDirPath).stream().forEach(file -> {
           FileInfoDto fileInfoDto = new FileInfoDto();
           fileInfoDto.setFullFileName(file.getName());
           fileInfoDto.setFileExtension(FileUtil.getFileExtension(file.getName()));
           fileInfoDto.setFileNm(FileUtil.getFileNameWithoutExtension(file.getName()));
           fileInfoDto.setFileUrl(FileUtil.getUrlPath(file.getName(),vComp.getProgramBasePath().replace(vComp.getFileServerBasePath(),""), dto.getPgmId(), vComp.EXEC_RESULT_DIR, dto.getRsltDirId()));
           resultFiles.add(fileInfoDto);
        });

        dto.setResultFiles(resultFiles);
    }
}
