package com.hscmt.simulation.dataset.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.InpFileUtil;
import com.hscmt.common.util.ShpFileUtil;
import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.dataset.comp.DatasetFileManager;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetTrendDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureSearchDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkJsonResponse;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkUrlResponse;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkVisResponse;
import com.hscmt.simulation.dataset.repository.DatasetRepository;
import com.hscmt.simulation.dataset.repository.MeasureDatasetDetailRepository;
import com.hscmt.simulation.dataset.repository.MeasureListRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
@SimulationTx(readOnly = true)
public class DatasetViewService {


    private final DatasetRepository datasetRepository;
    private final MeasureDatasetDetailRepository measureDatasetDetailRepository;
    private final MeasureListRepository measureListRepository;
    private final DatasetFileManager fileManager;

    /* 계측데이터셋 데이터 조회 */
    @Cacheable(
            value = CacheConst.CACHE_1MIN,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#dsId, #p1.cyclCd, #p1.searchStrtDttm, #p1.searchEndDttm)"
    )
    public MeasureDatasetTrendDto getMeasureDataset (String dsId, MeasureSearchDto searchDto) {
        MeasureDatasetTrendDto result = new MeasureDatasetTrendDto();
        List<MeasureDatasetDetailDto> targetTags = measureDatasetDetailRepository.findAllByDsId(dsId);
        result.setTargetItems(targetTags);
        result.setData(measureListRepository.findMeasureList(targetTags, searchDto));
        return result;
    }

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#dsId, #fileExtension)"
    )
    public PipeNetworkVisResponse getPipeNetworkDataset (String dsId, FileExtension fileExtension) {
        List<File> files = fileManager.getDatasetFiles(dsId);
        /* 해당경로에 파일 없으면 시각화 못함 */
        if (files.isEmpty()) throw new RestApiException(FileErrorCode.FILE_NOT_FOUND);
        /* inp 파일 시각화 */
        if (fileExtension == FileExtension.INP) {
            return new PipeNetworkJsonResponse(new JSONObject(InpFileUtil.combineInpModel(files.getFirst())));
        } else if (fileExtension == FileExtension.SHP) {
            /* shp 파일 시각화 */
            if (datasetRepository.findDatasetDtoById(dsId, DatasetType.PIPE_NETWORK) instanceof PipeNetworkDatasetDto d) {
                File shpFile = files.stream().filter(f -> FileUtil.getFileExtension(f.getName()).equals("shp")).findFirst().orElse(null);
                if (shpFile == null) return null;
                return new PipeNetworkJsonResponse(ShpFileUtil.convertShpToGeoJson(shpFile.getAbsolutePath(), d.getCrsyTypeCd().getEpsgName()));
            } else {
                return null;
            }
        } else if (fileExtension == FileExtension.TIFF) {
            /* tiff 파일 url 반환 */
            return new PipeNetworkUrlResponse(fileManager.convertFileUrl(dsId, files.getFirst().getName()));
        } else {
            return null;
        }
    }
}
