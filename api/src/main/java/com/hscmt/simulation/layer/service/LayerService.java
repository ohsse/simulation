package com.hscmt.simulation.layer.service;

import com.hscmt.common.dto.FileInfoDto;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.StringUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.layer.domain.Layer;
import com.hscmt.simulation.layer.dto.LayerDto;
import com.hscmt.simulation.layer.dto.LayerFeatureDto;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.layer.event.LayerEventPublisher;
import com.hscmt.simulation.layer.repository.LayerListRepository;
import com.hscmt.simulation.layer.repository.LayerRepository;
import com.hscmt.simulation.program.domain.ProgramResult;
import com.hscmt.simulation.program.repository.ProgramResultRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
@Slf4j
public class LayerService {
    private final LayerEventPublisher publisher;
    private final LayerRepository repository;
    private final LayerListRepository layerListRepository;
    private final VirtualEnvironmentComponent vComp;
    private final ProgramResultRepository programResultRepository;


    public List<LayerDto> findAllLayer() {
        return repository.findAllLayers(null);
    }

    public LayerDto findLayerById(String layerId) {
        LayerDto layerDto = repository.findLayerDtoById(layerId);
        String layerDir = FileUtil.getDirPath(vComp.getLayerBasePath(), layerId);
        List<File> files = FileUtil.getOnlyFilesInDirectory(layerDir);

        List<FileInfoDto> fileList = files.stream().map(file -> {
                    FileInfoDto fileInfoDto = new FileInfoDto();
                    fileInfoDto.setFullFileName(file.getName());
                    fileInfoDto.setFileExtension(FileUtil.getFileExtension(file.getName()));
                    fileInfoDto.setFileNm(FileUtil.getFileNameWithoutExtension(file.getName()));
                    fileInfoDto.setFileUrl(FileUtil.getUrlPath(file.getName(), vComp.getLayerBasePath().replace(vComp.getFileServerBasePath(), ""), layerId));
                    return fileInfoDto;
                })
                .toList();
        layerDto.setFileList(fileList);
        return layerDto;
    }

    public List<LayerFeatureDto> findLayerFeaturesByIdAndExtent(String layerId, Double minX, Double minY, Double maxX, Double maxY) {
        return layerListRepository.findLayerFeatures(layerId, minX, minY, maxX, maxY);
    }


//    private JSONObject convertLayerListToGeoJson (List<LayerFeatureDto> list) {
//        List<Map<String, Object>> maplist = list.stream().map(d -> {
//            Map<String, Object> m = new java.util.LinkedHashMap<>();
//            m.put("layerId", d.getLayerId());
//            m.put("ftype", d.getFtype());
//            m.put("fid", d.getFid());
//            m.put("colorStr", d.getColorStr());
//            m.put("property", d.getProperty());
//            // 핵심: geometry는 WKT 문자열로 넣어준다
//            m.put("gmtrVal", GeometryUtil.convertGeometryToWkt(d.getGeom()));
//            return m;
//        }).toList();
//
//        return GeometryUtil.listToGeoJSON(maplist, "gmtrVal", "WKT");
//    }


    public LayerFeatureDto findLayerFeatureInfo(String layerId, Long fid) {
        return layerListRepository.findLayerFeatureInfo(layerId, fid);
    }

    @SimulationTx
    public void deleteLayer(String layerId) {
        /* 레이어 삭제 이벤트 발행 */
        repository.findById(layerId)
                .ifPresent(findLayer -> {
                    publisher.deleteAndPublish(findLayer);
                });
    }

    @SimulationTx
    public String upsertLayer(List<MultipartFile> files, LayerUpsertDto dto) {
        String layerId = dto.getLayerId();
        Layer layer;

        /* 파일 검증 */
        validateFiles(files);

        if (layerId == null || layerId.isEmpty()) {
            /* 신규등록일 때는 파일이 존재 해야 한다. */
            if (files == null || files.size() == 0) {
                throw new RestApiException(FileErrorCode.FILE_NOT_FOUND);
            }
            /* 저장 후 이벤트 발행 */
            layer = publisher.saveAndPublish(new Layer(dto));
        } else {
            layer = repository.findById(layerId).orElse(null);
            publisher.updateAndPublish(layer, dto);
        }

        /* 파일 업로드 */
        uploadFiles(files, layer.getLayerId());

        return layer.getLayerId();
    }

    private void uploadFiles(List<MultipartFile> files, String layerId) {
        if (files == null || files.isEmpty()) return;
        if (layerId == null || layerId.isEmpty()) return;

        String saveDirPath = FileUtil.getDirPath(vComp.getLayerBasePath(), layerId);
        Set<String> uploadEnableExtensions = FileUtil.extractValidExtensions(files, FileExtension.SHP);

        files.stream()
                .filter(file -> uploadEnableExtensions.contains(FileUtil.getFileExtension(file.getOriginalFilename())))
                .forEach(file -> {
                    try {
                        FileUtil.uploadMultiPartFile(file, saveDirPath);
                    } catch (IOException e) {
                        throw new RestApiException(FileErrorCode.FILE_UPLOAD_ERROR);
                    }
                });
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> requiredExtensions = FileExtension.SHP.getRequiredExtensions();

        // 필수 확장자를 가진 파일들만 필터링
        Map<String, String> extToBaseName = FileUtil.getMatchedFileExtension(files, requiredExtensions);

        if (!extToBaseName.keySet().containsAll(requiredExtensions)) {
            throw new RestApiException(FileErrorCode.MISSING_REQUIRED_SHAPE_FILE);
        }

        Set<String> baseNames = extToBaseName.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (baseNames.size() != 1) {
            throw new RestApiException(FileErrorCode.MISMATCHED_SHP_FILENAMES);
        }
    }

    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void saveResultLayer(List<ProgramResult> layerResults) {
        if (layerResults.isEmpty()) return;

        List<String> allIds = layerResults.stream().map(ProgramResult::getRsltId).toList();
        List<Layer> exists = repository.findAllById(allIds);
        Set<String> existIds = new HashSet<>(exists.stream().map(Layer::getLayerId).toList());

        List<Layer> newLayerList = layerResults
                .stream()
                .filter(r -> !existIds.contains(r.getRsltId()))
                .map(Layer::fromShpResult)
                .toList();

        if (!newLayerList.isEmpty()) {
            for (Layer layer : newLayerList) {
                try {
                    repository.upsertLayerByResult(
                            layer.getLayerId(),
                            layer.getLayerNm(),
                            layer.getLayerDesc(),
                            layer.getRgstId(),
                            layer.getRgstDttm(),
                            layer.getMdfId(),
                            layer.getMdfDttm()
                    );

                    publisher.saveAndPublish(layer);
                } catch (Exception e) {
                    log.error("save result layer error : {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void downloadLayer(String layerId, HttpServletResponse response) {
        repository.findById(layerId)
                .ifPresent(findLayer -> {
                    String layerNm = findLayer.getLayerNm();
                    String cd = StringUtil.contentDispositionFileName(layerNm + ".zip");
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, cd);
                    response.setContentType(String.valueOf(MediaType.parseMediaType("application/zip")));
                    String dirPath = FileUtil.getDirPath(vComp.getLayerBasePath(), layerId);

                    try (
                            OutputStream os = response.getOutputStream();
                            ZipOutputStream zos = new ZipOutputStream(os);
                    ) {
                        FileUtil.mergeToZip(Paths.get(dirPath), null, zos);
                    } catch (Exception e) {
                        log.error("zip download error : {} ", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
    }
}
