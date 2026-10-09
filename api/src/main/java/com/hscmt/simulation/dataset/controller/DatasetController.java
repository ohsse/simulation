package com.hscmt.simulation.dataset.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.DatasetType;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.dataset.dto.DatasetDto;
import com.hscmt.simulation.dataset.dto.DatasetSearchDto;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.dataset.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "03. 데이터셋 관련 요청 명세", description = "데이터셋 등록|수정|삭제")
public class DatasetController extends CommonController {
    private final DatasetService datasetService;

    @UserRoleCheckRequired
    @Operation(summary = "데이터셋 등록 및 수정", description = "데이터셋 등록 및 수정")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping(value = "/dataset", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseObject<Void>> saveDataset (
            @Parameter(
                    name = "info",
                    description = "데이터셋 정보",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DatasetUpsertDto.class))
            )
            @RequestPart(name = "info") DatasetUpsertDto info,
            @Parameter(
                    name = "files",
                    description = "파일 정보",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, array = @ArraySchema(schema = @Schema(implementation = MultipartFile.class, type = "string", format = "binary")))
            )
            @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) {
        datasetService.saveDataset(info, files);
        return getResponseEntity();
    }
    
    @Operation(summary = "데이터셋 목록 조회", description = "데이터셋 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping(value = "/datasets")
    public ResponseEntity<ResponseObject<List<DatasetDto>>> getDatasetList (@ModelAttribute DatasetSearchDto dto) {
        return getResponseEntity(datasetService.getDatasetList(dto));
    }

    @Operation(summary = "데이터셋 상세 조회", description = "데이터셋 정보 상세 조회")
    @ApiResponses(value ={
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping(value = "/dataset/{dsId}")
    public ResponseEntity<ResponseObject<DatasetDto>> getDatasetById(@PathVariable(name = "dsId") String dsId, @RequestParam(name = "dsTypeCd") DatasetType dsTypeCd
    ) {
        return getResponseEntity(datasetService.findDataset(dsId, dsTypeCd));
    }

    @UserRoleCheckRequired
    @Operation(summary = "데이터셋 삭제", description = "데이터셋 삭제")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @DeleteMapping("/dataset/{dsId}")
    public ResponseEntity<ResponseObject<Void>> deleteDatasetById (@PathVariable(name = "dsId") String dsId) {
        datasetService.delete(dsId);
        return getResponseEntity();
    }

    @Operation(summary = "데이터셋다운로드", description = "데이터셋 다운로드")
    @ApiResponses({
            @ApiResponse(description = "성공",responseCode = "200")
    })
    @GetMapping("/dataset-download/{dsId}")
    @UncheckedJwtToken
    public void datasetDownload (@PathVariable(name = "dsId") String dsId, HttpServletResponse response) {
        datasetService.download(dsId, response);
    }
}
