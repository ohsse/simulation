package com.hscmt.simulation.dataset.service;

import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDetailDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetDto;
import com.hscmt.simulation.dataset.repository.MeasureDatasetRepository;
import com.hscmt.simulation.dataset.repository.MeasureListRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasureDatasetService {

    private final MeasureDatasetRepository repository;
    private final MeasureListRepository dataRepository;
    private final VirtualEnvironmentComponent vComp;

    private final String LABEL = "계측일시";
    private final String FIELD = "msrmDttm";

    public void createMeasureDatasetFile (String dsId) {
        /* 넘겨받은 데이터셋 ID 를 통해 상세 정보 조회 */
        MeasureDatasetDto target = repository.findDatasetDetailInfoByDsId(dsId)
                .stream().anyMatch(t -> t.getDsId().equals(dsId)) ? repository.findDatasetDetailInfoByDsId(dsId).getFirst() : null;

        if (target == null) return;

        LocalDateTime searchStartDttm;
        LocalDateTime searchEndDttm;

        if (target.getRltmYn() == YesOrNo.Y) {
            searchEndDttm = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).minus(5, ChronoUnit.MINUTES);
            searchStartDttm = searchEndDttm.minus(target.getInqyTerm(), target.getTermTypeCd().getUnit());
        } else {
            searchStartDttm = target.getStrtDttm();
            searchEndDttm = target.getEndDttm();
        }

        if (searchStartDttm == null && searchEndDttm == null) return;

        List<MeasureDatasetDetailDto> tagList = target.getDetailItems();

        /* 계측데이터셋 데이터 */
        List<Map<String, Object>> datasetDataList = dataRepository.findAllDatasetDataList(tagList, searchStartDttm, searchEndDttm);

        /* 데이터셋 파일 저장 폴더 경로 */
        String datasetFileDir = FileUtil.getDirPath(vComp.getDatasetBasePath(), dsId);

        FileUtil.createFile(datasetFileDir);

        if (target.getFileXtns() == FileExtension.XLSX) {
            /* 엑셀파일 만들기 */
            createExcel (datasetDataList, datasetFileDir, target.getDsNm());
        } else if (target.getFileXtns() == FileExtension.CSV) {
            /* csv 파일 만들기 */
            createCsv (datasetDataList, datasetFileDir, target.getDsNm());
        } else {
            throw new RestApiException(FileErrorCode.INVALID_MEASURE_FILE_EXTENSION);
        }
    }

    /* 엑셀파일 생성 */
    private void createExcel (List<Map<String, Object>> list, String dirPath, String fileName) {
        String orgFilePath = FileUtil.getFilePath(fileName + ".xlsx", dirPath);
        String tempFilePath = orgFilePath + ".temp";
        List<String> headers = getHeaders(list);

        try (
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream fos = new FileOutputStream(tempFilePath);
        ) {
            log.info("create excel temp file start : {}", tempFilePath);

            /* sheet 생성 */
            Sheet sheet = workbook.createSheet();

            for (int i = 0; i < list.size(); i ++) {

                Row row = sheet.createRow(i);
                if (i == 0) {
                    for (int j = 0; j < headers.size(); j ++) {
                        row.createCell(j).setCellValue(headers.get(j));
                    }
                } else {
                    Map<String, Object> data = list.get(i-1);
                    for (int j = 0; j < headers.size(); j ++) {
                        String header = convertMsrmHeaderKey(headers.get(j));
                        String value = data.get(header) == null ? "" : data.get(header).toString();
                        row.createCell(j).setCellValue(value);
                    }
                }
            }

            /* 엑셀파일 출력 */
            workbook.write(fos);

            log.info("create excel temp file end");
        } catch (IOException e) {
            log.error("create excel temp file error : {}", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_WRITE_ERROR);
        }

        moveFileAtomic(tempFilePath, orgFilePath);
    }

    /* csv 파일 생성 */
    private void createCsv (List<Map<String, Object>> list, String dirPath, String fileName) {
        String orgFilePath = FileUtil.getFilePath(fileName + ".csv", dirPath);
        String tempFilePath = orgFilePath + ".temp";
        List<String> headers = getHeaders(list);

        try (
                Writer writer = new FileWriter(tempFilePath, Charset.forName("EUC-KR"));
                CSVWriter csvWriter = new CSVWriter(writer);
        ) {
            /* 헤더 그리기 */
            csvWriter.writeNext(headers.toArray(new String[headers.size()]));

            /* 데이터 쓰기 */
            for (int i = 0; i < list.size(); i ++) {
                Map<String, Object> data = list.get(i);
                csvWriter.writeNext(headers.stream().map(h -> data.get(convertMsrmHeaderKey(h)) == null ? "" : String.valueOf(data.get(convertMsrmHeaderKey(h)))).toArray(String[]::new));
            }

        } catch (IOException e) {
            log.error("create csv temp file error : {}", e.getMessage());
            throw new RestApiException(FileErrorCode.FILE_WRITE_ERROR);
        }

        moveFileAtomic(tempFilePath, orgFilePath);
    }

    /* 파일 이동 원자성 복사 */
    private void moveFileAtomic (String tempFilePath, String orgFilePath) {
        log.info(" temp file rename start  temp : {}, org : {} ", tempFilePath, orgFilePath);
        try {
            FileUtil.moveFile(tempFilePath, orgFilePath);
        } catch (Exception e) {
            throw new RestApiException(FileErrorCode.FILE_WRITE_ERROR);
        }
        log.info(" temp file rename end");
    }


    private List<String> getHeaders (List<Map<String, Object>> list) {
        Set<String> set = new HashSet<>(list.getFirst().keySet());
        set.remove(FIELD);
        List<String> result = new ArrayList<>();
        result.add(LABEL);
        result.addAll(new ArrayList<>(set));
        return result;
    }

    private String convertMsrmHeaderKey (String header) {
        return header.equals(LABEL) ? FIELD : header;
    }

}
