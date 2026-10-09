package com.hscmt.simulation.library.service;

import com.hscmt.common.util.PyPackageUtil;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.library.domain.Library;
import com.hscmt.simulation.library.dto.LibraryDto;
import com.hscmt.simulation.library.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class LibraryService {
    private final LibraryRepository libraryRepository;
    private final LibraryFileManager manager;
    /**
     * 패키지 등록은 물리경로에 파일을 저장하는 동작만 해서
     * domain event로 분리하지 않고
     * 트랜잭션 내에서 함께 처리하도록 함.
     *
     * event로 분리했을 때, IOException 같이 파일저장에서 문제가 생기면
     * rollback 처리가 더 어렵다.
     */
    @SimulationTx
    public void registerPythonLibraries (List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;
        List<MultipartFile> enableFiles = manager.filterWheelFiles(files);
        /* 엔터티정보 추출 */
        List<Library> libraries = manager.convertEntitiesByFile(files);
        /* 라이브러리 목록 저장 */
        libraryRepository.saveAll(libraries);
        /* 파일 등록 */
        manager.uploadLibraries(enableFiles);
    }

    /* 라이브러리 패키지 삭제 */
    @SimulationTx
    public void deleteLibrary (String lbrId) {
        libraryRepository.findById(lbrId)
                .ifPresent(findLibrary -> {
                    manager.deleteLibrary(findLibrary.getOrtxFileNm());
                    libraryRepository.delete(findLibrary);
                });
    }

    /* 라이브러리 패키지 다건 삭제 */
    @SimulationTx
    public void deleteLibraries (List<String> lbrIds) {
        List<Library> libraries = libraryRepository.findAllById(lbrIds);
        for (Library library : libraries) {
            manager.deleteLibrary(library.getOrtxFileNm());
        }
        libraryRepository.deleteAll(libraries);
    }

    /* 파이썬 패키지 목록 조회 */
    public List<LibraryDto> findAllPyLibraries (String pyVrsn) {
        List<LibraryDto> list = libraryRepository.findAllLibraries();

        if (pyVrsn != null && !pyVrsn.isEmpty()) {
            return list
                    .stream()
                    .filter(x -> PyPackageUtil.isAbleSelectedPythonVersion(x.getOrtxFileNm(), pyVrsn))
                    .toList();
        }

        return list;
    }
}
