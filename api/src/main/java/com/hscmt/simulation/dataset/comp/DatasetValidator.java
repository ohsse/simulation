package com.hscmt.simulation.dataset.comp;

import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.exception.RestApiException;
import com.hscmt.common.exception.error.FileErrorCode;
import com.hscmt.common.util.FileUtil;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetUpsertDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DatasetValidator {

    private static final Set<FileExtension> VALID_ONLY_PIPE_NETWORK_FILE_EXTENSIONS = Set.of(FileExtension.INP, FileExtension.SHP, FileExtension.TIFF);


    /* 데이터셋 ID 없다면 신규 : 신규인데 파일이 없으면 에러 */
    public void isNewDataset(DatasetUpsertDto dto, List<MultipartFile> files) {
        if ((dto.getDsId() == null || dto.getDsId().isEmpty()) && (files == null || files.isEmpty())) {
            throw new RestApiException(FileErrorCode.FILE_NOT_FOUND);
        }
    }

    /* 파일 확장자 검증 */
    public void validateFileExtension (DatasetUpsertDto dto) {
        FileExtension fileXtns = dto.getFileXtns();

        if (dto instanceof PipeNetworkDatasetUpsertDto) {
            if (!VALID_ONLY_PIPE_NETWORK_FILE_EXTENSIONS.contains(fileXtns)) {
                throw new RestApiException(FileErrorCode.INVALID_FILE_EXTENSION);
            }
        } else if (dto instanceof UserDefinitionDatasetUpsertDto) {
            if (VALID_ONLY_PIPE_NETWORK_FILE_EXTENSIONS.contains(fileXtns)) {
                throw new RestApiException(FileErrorCode.INVALID_FILE_EXTENSION);
            }
        }
    }

    /* 관망데이터셋 파일 검증 */
    public void validatePipeNetworkFiles (DatasetUpsertDto dto, List<MultipartFile> files) {
        FileExtension fileXtns = dto.getFileXtns();

        if (files != null && !files.isEmpty()) {
            if (( fileXtns == FileExtension.INP || fileXtns == FileExtension.TIFF ) && files.size() > 1) {
                throw new RestApiException(FileErrorCode.INVALID_FILE_COUNT);
            }
            if (fileXtns == FileExtension.SHP) {

                List<String> requiredExts = FileExtension.SHP.getRequiredExtensions();

                // 필수 확장자를 가진 파일들만 필터링
                Map<String, String> extToBaseName = FileUtil.getMatchedFileExtension(files, requiredExts);

                // 필수 확장자가 모두 있는지 확인
                if (!extToBaseName.keySet().containsAll(requiredExts)) {
                    throw new RestApiException(FileErrorCode.MISSING_REQUIRED_SHAPE_FILE);
                }

                // BaseName이 전부 동일한지 확인
                Set<String> baseNames = new HashSet<>(extToBaseName.values());
                if (baseNames.size() != 1) {
                    throw new RestApiException(FileErrorCode.MISMATCHED_SHP_FILENAMES);
                }
            }
        }
    }
}
