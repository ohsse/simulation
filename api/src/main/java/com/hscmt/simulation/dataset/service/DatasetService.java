package com.hscmt.simulation.dataset.service;

import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.StringUtil;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dataset.comp.DatasetFileManager;
import com.hscmt.simulation.dataset.comp.DatasetValidator;
import com.hscmt.simulation.dataset.domain.*;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailUpsertDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetUpsertDto;
import com.hscmt.simulation.dataset.event.DatasetEventPublisher;
import com.hscmt.simulation.dataset.repository.DatasetRepository;
import com.hscmt.simulation.dataset.repository.MeasureDatasetDetailRepository;
import com.hscmt.simulation.dataset.repository.WaternetTagRepository;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.program.repository.ProgramInputFileRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class DatasetService {
    private final DatasetRepository datasetRepository;
    private final DatasetFileManager fileManager;
    private final DatasetValidator validator;
    private final MeasureDatasetDetailRepository measureDatasetDetailRepository;
    private final WaternetTagRepository waternetTagRepository;
    private final DatasetEventPublisher publisher;
    private final ProgramInputFileRepository programInputFileRepository;

    /* 데이터셋 저장 */
    @SimulationTx
    public void saveDataset (DatasetUpsertDto dto, List<MultipartFile> files) {
        if (dto instanceof PipeNetworkDatasetUpsertDto pipeNetworkDatasetUpsertDto ) {
            savePipeNetworkDataset(pipeNetworkDatasetUpsertDto, files);
        } else if ( dto instanceof UserDefinitionDatasetUpsertDto userDefinitionDatasetUpsertDto ) {
            saveUserDefinitionDataset(userDefinitionDatasetUpsertDto, files);
        } else if ( dto instanceof MeasureDatasetUpsertDto measureDatasetUpsertDto ) {
            saveMeasureDataset(measureDatasetUpsertDto);
        }
    }

    /* 관망 데이터셋 저장 */
    public void savePipeNetworkDataset (PipeNetworkDatasetUpsertDto dto, List<MultipartFile> files ) {
        /* 데이터셋 ID */
        String dsId = dto.getDsId();
        /* 데이터셋 확장자 체크 */
        validator.validateFileExtension(dto);
        /* 데이터셋 ID 없다면 신규 : 신규인데 파일이 없으면 에러 */
        validator.isNewDataset(dto, files);
        /* 유효확장자 목록 추출 */
        Set<String> uploadExtensions = FileUtil.extractValidExtensions(files, dto.getFileXtns());
        /* 유효데이터셋 파일목록 검증 */
        validator.validatePipeNetworkFiles(dto, files);
        Dataset dataset;
        /* 신규데이터셋 */
        if (dsId == null || dsId.isEmpty()) {
            if (!uploadExtensions.isEmpty()) dataset = publisher.saveAndPublish(new PipeNetworkDataset(dto));
            else dataset = null;
        }
        /* 기존데이터셋 */
        else {
            dataset = datasetRepository.findDatasetById(dsId, DatasetType.PIPE_NETWORK);
            publisher.updateAndPublish(dataset, dto);
        }
        /* 파일 업로드 */
        if (dataset != null) {
            fileManager.uploadValidFiles(files, uploadExtensions, dataset.getDsId());
        }

    }

    /* 사용자 정의 데이터셋 */
    public void saveUserDefinitionDataset (UserDefinitionDatasetUpsertDto dto, List<MultipartFile> files) {
        String dsId = dto.getDsId();
        /* 데이터셋 확장자 검증 */
        validator.validateFileExtension(dto);
        /* 신규데이터셋 검증 */
        validator.isNewDataset(dto, files);
        /* 업로드 가능 확장자 추출 */
        Set<String> uploadExtensions = FileUtil.extractValidExtensions(files, dto.getFileXtns());
        /* 신규데이터셋 */
        Dataset dataset;
        if (dsId == null || dsId.isEmpty()) {
            if (!uploadExtensions.isEmpty()) dataset = publisher.saveAndPublish(new UserDefinitionDataset(dto));
            else dataset = null;
        }
        /* 기존데이터셋 */
        else {
            dataset = datasetRepository.findDatasetById(dsId, DatasetType.USER_DEF);
            publisher.updateAndPublish(dataset, dto);
        }
        /* 파일 업로드 */
        if (dataset != null){
            fileManager.uploadValidFiles(files, uploadExtensions, dataset.getDsId());
        }
    }

    /* 계측데이터셋 저장 */
    public void saveMeasureDataset (MeasureDatasetUpsertDto dto) {
        String dsId = dto.getDsId();
        Dataset dataset;
        /* 계측데이터셋 저장 */
        if (dsId == null || dsId.isEmpty()) {
            /**
             * 다른 데이터셋들은 등록할 때 파일까지 등록하는 것이 한 세트라서
             * 이벤트를 사용하지 않지만
             *
             * 계측데이터셋은 신규등록일 경우
             *
             * Spring batch 쪽으로 신규데이터셋 파일을 생성하도록 명령한다.
             */
            dataset = publisher.saveAndPublish(new MeasureDataset(dto));
        } else {
            dataset = datasetRepository.findDatasetById(dsId, DatasetType.MEASURE);
            publisher.updateAndPublish(dataset, dto);
        }

        /* 저장전에 전체 항목 삭제 */
        measureDatasetDetailRepository.deleteAllByDsId(dataset.getDsId());
        for (MeasureDatasetDetailUpsertDto tag : dto.getDetailItems()) {
            /* 워터넷 태그 등록 */
            waternetTagRepository.findById(tag.getTagSn())
                    .ifPresentOrElse(saveTag -> saveTag.changeTagDesc(tag.getTagDesc())
                            , () -> waternetTagRepository.save(new WaternetTag(tag)));
            /* 계측데이터셋 상세항목 등록 */
            measureDatasetDetailRepository.save(new MeasureDatasetDetail(dataset, tag));
        }
    }

    /* 데이터셋 목록 전체 조회 */ 
    public List<DatasetDto> getDatasetList (DatasetSearchDto dto) {
        List<DatasetDto> baseInfo = datasetRepository.findAllDatasets(dto);
        List<DatasetDto> additionalInfo = programInputFileRepository.groupingForDataset(dto);
        return mergeData (baseInfo, additionalInfo);
    }

    /* 데이터셋 상세 조회 */
    public DatasetDto findDataset (String dsId, DatasetType dsTypeCd) {
        return datasetRepository.findDatasetDtoById(dsId, dsTypeCd);
    }

    /* 데이터셋 기본정보 + 데이터셋 프로그램 정보 병합 */
    public List <DatasetDto> mergeData (List <DatasetDto> baseInfo, List <DatasetDto> additionalInfo) {
        Map <String, DatasetDto> baseMap = baseInfo.stream().collect(toMap(DatasetDto::getDsId, Function.identity()));
        for (DatasetDto additional : additionalInfo) {
            DatasetDto base = baseMap.get(additional.getDsId());
            if (base != null) {
                base.setPrograms(additional.getPrograms());
            }
        }
        return new ArrayList <>(baseMap.values());
    }

    @SimulationTx
    public void delete (String dsId) {
        datasetRepository.findById(dsId)
                .ifPresent(deleteDataset -> {
                    publisher.deleteAndPublish(deleteDataset);
                });
    }

    public void download (String dsId, HttpServletResponse response) {
        datasetRepository.findById(dsId)
                .ifPresent(e -> {
                    String fileName  = e.getDsNm() + ".zip";
                    String cd = StringUtil.contentDispositionFileName(fileName);
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, cd);
                    response.setContentType(String.valueOf(MediaType.parseMediaType("application/zip")));

                    try (
                            OutputStream os = response.getOutputStream();
                            ZipOutputStream zos = new ZipOutputStream(os);
                    ) {
                        FileUtil.mergeToZip(Paths.get(fileManager.getDsDirId(dsId)), null, zos);
                    } catch (Exception ex) {
                        log.error("zip download error : {} ", ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                });
    }

    @SimulationTx
    public void updateGrpIdToNull (String grpId) {
        datasetRepository.updateGrpIdToNull(grpId);
    }

    @SimulationTx
    public void updateGrpIdToNull (List<String> grpIds) {
        datasetRepository.updateGrpIdToNull(grpIds);
    }

    @SimulationTx
    public void updateGroupItems (List<GroupItemUpsertDto> items) {
        for (GroupItemUpsertDto item : items) {
            if (item instanceof DatasetUpsertDto datasetUpsertDto) {
                datasetRepository.findById(datasetUpsertDto.getDsId())
                        .ifPresent(findDataset -> findDataset.changeGrpInfo(item.getGrpId(), item.getSortOrd()));
            }
        }
    }
}