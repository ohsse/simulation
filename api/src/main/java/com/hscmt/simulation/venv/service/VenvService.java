package com.hscmt.simulation.venv.service;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.venv.comp.VenvLibraryManager;
import com.hscmt.simulation.venv.comp.VenvManager;
import com.hscmt.simulation.venv.domain.VirtualEnvironment;
import com.hscmt.simulation.venv.dto.*;
import com.hscmt.simulation.venv.error.VenvErrorCode;
import com.hscmt.simulation.venv.event.VenvEventPublisher;
import com.hscmt.simulation.venv.repository.VenvRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class VenvService {
    private final VenvRepository venvRepository;
    private final VenvEventPublisher publisher;
    private final VirtualEnvironmentComponent comp;
    private final VenvLibraryManager venvLibraryManager;
    private final VenvManager venvManager;

    /* 가상환경 생성 */
    @SimulationTx
    public String createVirtualEnvironment (VenvCreateDto dto) {
        String pyVrsn = dto.getPyVrsn();
        if (comp.isEnablePyVrsn(pyVrsn)) {
            /* 엔터티 생성과 이벤트 발행을 함께 한다. */
            return publisher.saveAndPublish(new VirtualEnvironment(dto), dto.getLbrIds()).getVenvId();
        } else {
            throw new RestApiException(VenvErrorCode.INVALID_PYTHON_VERSION);
        }
    }

    @SimulationTx
    public String updateVirtualEnvironment (VenvUpdateDto dto) {
        venvRepository.findById(dto.getVenvId())
                .ifPresent( findVenv -> {
                    publisher.updateAndPublish( findVenv, dto );
                });

        return dto.getVenvId();
    }

    @SimulationTx
    public String deleteVirtualEnvironment (String venvId) {
        venvRepository.findById(venvId)
                .ifPresent(findVenv -> publisher.deleteAndPublish(findVenv));

        return venvId;
    }

    /* 전체가상환경 조회 */
    public List<VenvDto> findAllVenvs (String pyVrsn) {
        return venvRepository.findAllVenvs(pyVrsn);
    }

    /* 가상환경 라이브러리 포함 상세정보 조회 */
    public VenvDto findVenvInfoWithLibrary(String venvId) {
        VenvDto targetVenv = venvRepository.findVenvById(venvId);
        List<VenvLbrDto> realLibraries = venvLibraryManager.findAllVenvLibrariesByVenvId(venvId);
        targetVenv.addLbrs(realLibraries);
        return targetVenv;
    }

    /* 특정가상환경 파이썬 패키지 물리파일 목록 조회 */
    public VenvPackageDto findVenvAllPyPackageFiles (String venvId) {
        return venvManager.findAllRealFilesInVenvPackageDirectory(venvId);
    }

    /* 파일업로드 in 파이썬패키지 폴더 */
    public void uploadFilesInPackageDirectory (String venvId, String targetPath, List<MultipartFile> files) {
        venvManager.uploadFilesInPackageDirectory(venvId, targetPath, files);
    }

    public void deletePackageFile (String venvId, String filePath) {
        venvManager.deletePackageFile(venvId, filePath);
    }
}
