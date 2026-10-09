package com.hscmt.simulation.venv.comp;

import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.CommonErrorCode;
import com.hscmt.common.response.CommandResult;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.ProcessOutputParser;
import com.hscmt.common.util.ProcessUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.library.domain.Library;
import com.hscmt.simulation.library.repository.LibraryRepository;
import com.hscmt.simulation.venv.dto.VenvLbrDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@SimulationTx(readOnly = true)
public class VenvLibraryManager {
    private final LibraryRepository libraryRepository;
    private final VirtualEnvironmentComponent comp;

    public List<VenvLbrDto> findAllVenvLibrariesByVenvId (String venvId) {
        String venvPath = comp.getVenvBasePath() + File.separator + venvId;

        final String[] basePackages = new String[]{"pip", "setuptools", "wheel"};
        StringBuffer command = new StringBuffer(comp.getAnacondaActivatePath())
                .append(" ")
                .append(venvPath)
                .append(" && ")
                .append("pip list");

        List<VenvLbrDto> lbrs = new ArrayList<>();
        try {
            CommandResult result = ProcessUtil.runCommand(command.toString());
            if (result.getExitCode() == 0) {
                final String[] targetKeys = new String[]{"Package", "Version"};
                List<Map<String, String>> resultList = ProcessOutputParser.parseKeyValue(result.getOutputMessage(), targetKeys);
                if (resultList.size() != 0) {
                    for (Map<String, String> map : resultList) {
                        if (!Arrays.stream(basePackages).toList().contains(map.get(targetKeys[0]))) {
                            VenvLbrDto lbrDto = new VenvLbrDto();
                            lbrDto.setLbrNm(map.get(targetKeys[0]));
                            lbrDto.setLbrVrsn(map.get(targetKeys[1]));
                            lbrs.add(lbrDto);
                        }
                    }
                }
            } else {
                log.error(result.getErrorMessage());
                throw new RestApiException(CommonErrorCode.COMMAND_LINE_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RestApiException(CommonErrorCode.COMMAND_LINE_ERROR);
        }

        return lbrs;
    }

    /* 가상환경 경로에 패키지 설치 */
    public List<String> installVenvPackages (String venvPath, List<String> lbrIds) throws Exception{

        List<String> failedPackages = new ArrayList<>();

        if (lbrIds.size() > 0) {
            log.info("package {} 개 설치 시작 !", lbrIds.size());
            List<Library> libs = libraryRepository.findAllById(lbrIds);

            for (Library lbr : libs) {
                log.info(" {}-{} 설치시작 ", lbr.getLbrId(), lbr.getPyVrsn());

                String lbrDirPath = comp.getLibraryBasePath();
                String rootDir = FileUtil.getTargetRootDriveName(lbrDirPath);

                StringBuffer command = new StringBuffer(rootDir)
                        .append(" && ")
                        .append("cd ")
                        .append(lbrDirPath)
                        .append(" && ")
                        .append(comp.getAnacondaActivatePath())
                        .append(" ")
                        .append(venvPath)
                        .append(" && ")
                        .append("pip install --no-index --find-links=./ ")
                        .append(lbrDirPath + File.separator + lbr.getOrtxFileNm());

                CommandResult result = ProcessUtil.runCommand(command.toString());
                int exitCode = result.getExitCode();

                if (exitCode == 0) {
                    log.info(" {}-{} 패키지pip  설치 완료!", lbr.getLbrId(), lbr.getPyVrsn());
                    log.info(result.getOutputMessage());
                } else {
                    failedPackages.add(lbr.getOrtxFileNm());
                    log.error("패키지 설치 실패 : exitCode = {}, installCommand = {}", exitCode, command.toString());
                    log.error(result.getErrorMessage());
                }
            }
        }
        return failedPackages;
    }

    /* 가상환경 경로에 패키지 삭제 */
    public List<String> deleteVenvPackages (String venvPath, List<String> lbrNms) throws Exception{
        List<String> failedPackages = new ArrayList<>();
        if ( lbrNms.size() > 0 ) {
            log.info(" package {} 개 삭제 시작 ", lbrNms.size() );
            for (String lbrNm : lbrNms) {
                log.info(" {} 패키지 삭제 시작", lbrNm);
                StringBuffer command = new StringBuffer(comp.getAnacondaActivatePath())
                        .append(" ")
                        .append(venvPath)
                        .append(" && ")
                        .append("pip uninstall ")
                        .append(lbrNm)
                        .append(" --y")
                        ;

                int exitCode = ProcessUtil.runCommand(command.toString()).getExitCode();

                if (exitCode == 0) {
                    log.info ("{} 패키지 삭제 완료", lbrNm);
                } else {
                    failedPackages.add(lbrNm);
                    log.error("{} 패키지 삭제 실패", lbrNm);
                }
            }
        }
        return failedPackages;
    }

}
