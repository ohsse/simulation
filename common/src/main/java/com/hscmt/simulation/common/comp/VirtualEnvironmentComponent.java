package com.hscmt.simulation.common.comp;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VirtualEnvironmentComponent {
    private final Environment env;
    @Getter private final List<String> ENABLE_PYTHON_VERSIONS =
            List.of("3.9.12", "3.9.13", "3.9.20", "3.10.11", "3.13");
    @Getter
    public final String REVISION_DIR = "revisions";
    @Getter
    public final String EXEC_RESULT_DIR = "results";
    @Getter
    private String anacondaActivatePath;
    @Getter
    private String datasetBasePath;
    @Getter
    private String libraryBasePath;
    @Getter
    private String venvBasePath;
    @Getter
    private String anacondaPythonEnvPath;
    @Getter
    private String programBasePath;
    @Getter
    private String layerBasePath;
    @Getter
    private String fileServerBasePath;

    /* 설치가능 파이썬 버전인지 확인 */
    public boolean isEnablePyVrsn(String pyVrsn) {
        return ENABLE_PYTHON_VERSIONS.contains(pyVrsn);
    }

    /* 기본 변수 세팅 */
    @PostConstruct
    public void setEnvironment() {
        this.anacondaActivatePath = env.getProperty("anaconda.activate-path");
        this.datasetBasePath = env.getProperty("dataset.base-path");
        this.libraryBasePath = env.getProperty("library.base-path");
        this.venvBasePath = env.getProperty("venv.base-path");
        this.anacondaPythonEnvPath = env.getProperty("anaconda.python-env-path");
        this.programBasePath = env.getProperty("program.base-path");
        this.layerBasePath = env.getProperty("layer.base-path");
        this.fileServerBasePath = env.getProperty("fileserver.base-path");
        /* 기본 폴더 생성 */        
        checkBaseDirs();
    }
    /* 기본폴더 생성 */
    public void checkBaseDirs() {
        /* 데이터셋 경로 확인 */
        File datasetDir = new File(datasetBasePath);
        if (!datasetDir.exists()) {
            boolean isCreate = datasetDir.mkdirs();
            log.info("datasetDir create result : {}", isCreate);
        }
        /* 라이브러리 경로 확인 */
        File libraryDir = new File(libraryBasePath);
        if (!libraryDir.exists()) {
            boolean isCreate = libraryDir.mkdirs();
            log.info("libraryDir create result : {}", isCreate);
        }
        /* 사용자 가상환경 기본경로 경로 확인 */
        File venvDir = new File(venvBasePath);
        if (!venvDir.exists()) {
            boolean isCreate = venvDir.mkdirs();
            log.info("userVenvDir create result : {}", isCreate);
        }
        /* 프로그램 경로 확인 */
        File programDir = new File(programBasePath);
        if (!programDir.exists()) {
            boolean isCreate = programDir.mkdirs();
            log.info("programDir create result : {}", isCreate);
        }
        /* 레이어 경로 확인 */
        File layerDir = new File(layerBasePath);
        if (!layerDir.exists()) {
            boolean isCreate = layerDir.mkdirs();
            log.info("layerDir create result : {}", isCreate);
        }
    }
}
