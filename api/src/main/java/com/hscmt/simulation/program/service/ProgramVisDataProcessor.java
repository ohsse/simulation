package com.hscmt.simulation.program.service;

import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.enumeration.InputFileType;
import com.hscmt.common.enumeration.VisTypeCd;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.util.*;
import com.hscmt.simulation.dataset.comp.DatasetFileManager;
import com.hscmt.simulation.program.comp.ProgramFileManager;
import com.hscmt.simulation.program.dto.PipeNetworkReportDto;
import com.hscmt.simulation.program.dto.ProgramInputFileDto;
import com.hscmt.simulation.program.dto.SectionRange;
import com.hscmt.simulation.program.dto.vis.*;
import com.hscmt.simulation.program.error.ProgramErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProgramVisDataProcessor {

    private final ProgramFileManager fileManager;
    private final DatasetFileManager dsFileManager;

    public VisResultItem makeVisData(VisTypeCd visTypeCd, VisSetupItem visSetupItem, List<File> resultFiles, List<ProgramInputFileDto> inputFiles) {
        try {
            return switch (visTypeCd) {
                case MAP -> makeMapData(visSetupItem, resultFiles, inputFiles);
                case CHART, GRID -> makeGridOrChartData(visSetupItem, resultFiles);
                case IMAGE -> makeImageData(visSetupItem, resultFiles);
            };
        } catch (Exception e) {
            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED);
        }
    }

    /* 관망해석 데이터 만들기 */
    public VisResultItem makeMapData(VisSetupItem visSetupItem, List<File> resultFiles, List<ProgramInputFileDto> inputFiles) throws Exception {
        if (visSetupItem instanceof MapSetupItemDto mapSetupItemDto) {
            VisualizationItem visItem = new VisualizationItem();
            visItem.setSetup(mapSetupItemDto);

            File layerFile= resultFiles.stream()
                    .filter(x -> x.getName().equals(mapSetupItemDto.getLayerFile())).findFirst().orElse(null);
            File reportFile = resultFiles.stream()
                    .filter(x -> x.getName().equals(mapSetupItemDto.getDataFile())).findFirst().orElse(null);

            List<SectionRange> displayOptions =  mapSetupItemDto.getResultDisplayOption();

            /* 속성명 세팅 */
            for (SectionRange sectionRange : displayOptions) {
                sectionRange.setAttributeName();
            }
            /* 관망해석용 결과 ITEM */
            MapResultItemDto result = new MapResultItemDto();
            /* 레이어 데이터 세팅 */
            if (layerFile != null) {
                result.setLayerData(InpFileUtil.combineInpModel(layerFile));
            } else {
                ProgramInputFileDto ipf = inputFiles.stream()
                        .filter(x -> x.getFileXtns() == FileExtension.INP && mapSetupItemDto.getLayerFile().equals(x.getTrgtNm() + ".inp") && x.getTrgtType() == InputFileType.DATASET)
                        .findFirst().orElse(null);

                if (ipf != null) {
                    List<File> dsFiles = dsFileManager.getDatasetFiles(ipf.getTrgtId());
                    dsFiles.stream().filter(x -> x.getName().equals(mapSetupItemDto.getLayerFile())).findFirst().ifPresent(f -> result.setLayerData(InpFileUtil.combineInpModel(f)));
                }

            }

            /* 보고서 파일이 존재한다면 */
            if (reportFile != null) {
                /* 보고서 데이터 세팅 */
                result.setReportResult(new PipeNetworkReportDto(displayOptions, reportFile));
            }

            List<DataKey> dataKeyList = mapSetupItemDto.getTargetList();

            if (dataKeyList != null && !dataKeyList.isEmpty()) {
                Map<String, List<ChartAndGridDataDto>> data = new LinkedHashMap<>();
                for (DataKey dataKey : dataKeyList) {
                    List<ChartAndGridDataDto> chartAndGridDataDtoList = new ArrayList<>();
                    List<String> valueList = getValueList(dataKey, resultFiles);
                    String matchedKey = matchedDataKey(dataKey);
                    if (!valueList.isEmpty()) {
                        for (String value : valueList) {

                            ChartAndGridDataDto chartAndGridDataDto = new ChartAndGridDataDto();
                            chartAndGridDataDto.setValue(value);
                            chartAndGridDataDto.setDataKey(matchedKey);
                            chartAndGridDataDtoList.add(chartAndGridDataDto);
                        }
                    }
                    data.put(matchedKey, chartAndGridDataDtoList);
                }
                result.setOverlayResult(data);
            }

            return result;
        }else {
            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED);
        }
    }

    /* 차트 or 그리드 데이터 만들기 */
    public VisResultItem makeGridOrChartData (VisSetupItem visSetupItem, List<File> resultFiles) throws Exception {
        if (visSetupItem instanceof GridSetupItemDto gridSetupItemDto) {
            return makeChartOrGridData(gridSetupItemDto.getTargetList(), resultFiles, "GRID");
        } else if (visSetupItem instanceof ChartSetupItemDto chartSetupItemDto) {
            return makeChartOrGridData(chartSetupItemDto.getTargetList(), resultFiles, "CHART");
        } else {
            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED);
        }
    }

    public List<String> getValueList (DataKey key, List<File> resultFiles) throws Exception {
        String fileName = key.getFileName();
        String sheetName = key.getSheetName();
        Integer headerIndex = key.getHeaderIndex();

        String fileExtension = FileUtil.getFileExtension(fileName);
        File resultFile = resultFiles.stream().filter(f -> f.getName().equals(fileName)).findFirst().orElse(null);

        if (resultFile != null) {
            if (fileExtension.endsWith("xlsx") || fileExtension.endsWith("xls")) {
                return ExcelUtil.read(resultFile.getAbsolutePath(),sheetName, headerIndex);
            } else if (fileExtension.endsWith("csv")) {
                return CsvUtil.read(resultFile.getAbsolutePath(), headerIndex);
            }
        } else {
            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED);
        }
        return new ArrayList<>();
    }

    public String matchedDataKey (DataKey key ) {
        return StringUtil.aggregateStringByAppender("|", key.getFileName(), key.getSheetName(), String.valueOf(key.getHeaderIndex()));
    }

    /* 차트|그리드 데이터 만들기 */
    public VisResultItem makeChartOrGridData (List<DataKey> targetList, List<File> resultFiles, String type) throws Exception {

        Map<String, List<ChartAndGridDataDto>> data = new LinkedHashMap<>();

        for (DataKey key : targetList) {
            List<ChartAndGridDataDto> dataList = new ArrayList<>();
            List<String> valueList = getValueList(key, resultFiles);
            if (!valueList.isEmpty()) {
                for (String value : valueList) {
                    ChartAndGridDataDto dto = new ChartAndGridDataDto();
                    dto.setDataKey(matchedDataKey(key));
                    dto.setValue(value);
                    dataList.add(dto);
                }
            }
            String dataKey = matchedDataKey(key);
            data.put(dataKey, dataList);
        }
        if (type.equals("GRID")) {
            GridResultItemDto result = new GridResultItemDto();
            result.setData(data);
            return result;
        } else {
            ChartResultItemDto result = new ChartResultItemDto();
            result.setData(data);
            return result;
        }
    }

    /* 이미지 데이터 만들기 */
    public VisResultItem makeImageData (VisSetupItem visSetupItem, List<File> resultFiles) {
        if (visSetupItem instanceof ImageSetupItemDto imageSetupItemDto) {
            String fileName = imageSetupItemDto.getFileName();
            File imageFile = resultFiles.stream().filter(f -> f.getName().equals(fileName)).findFirst().orElse(null);


            ImageResultItemDto result = new ImageResultItemDto();

            if (imageFile != null) {
               result.setFileUrl(fileManager.convertRequestUrlByFilePath(imageFile.getAbsolutePath()));
            }

            return result;
        } else {
            throw new RestApiException(ProgramErrorCode.PROGRAM_RESULTSET_CONVERT_DISPLAY_FAILED);
        }
    }
}