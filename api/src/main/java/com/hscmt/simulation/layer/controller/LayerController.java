package com.hscmt.simulation.layer.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.simulation.layer.dto.LayerDto;
import com.hscmt.simulation.layer.dto.LayerFeatureDto;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.layer.service.LayerService;
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
@Tag(name = "07. 레이어 관리", description = "사용자가 등록한 SHP를 통한 레이어 관리")
public class LayerController extends CommonController {

    private final LayerService service;

    @Operation(summary = "레이어 등록", description = "사용자정의 레이어 SHp파일 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping(value = "/layer", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<String>> uploadShapeLayer (
            @Parameter(
                    name = "files",
                    description = "SHP파일목록",
                    required = false,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MultipartFile.class)))
            )
            @RequestPart(name = "files", required = false) List<MultipartFile> files,
            @Parameter(
                    name = "info",
                    description = "레이어정보",
                    content = @Content(schema = @Schema(implementation = LayerUpsertDto.class))
            )
            @RequestPart(name = "info") LayerUpsertDto dto
    ) {
        return getResponseEntity(service.upsertLayer(files, dto));
    }

    @Operation(summary = "레이어 삭제", description = "사용자 정의 레이어 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @DeleteMapping("/layer/{layerId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deleteLayer(@PathVariable(name = "layerId") String layerId) {
        service.deleteLayer(layerId);
        return getResponseEntity();
    }
    
    @Operation(summary = "레이어 목록 조회", description = "사용자 정의 레이어 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/layers")
    public ResponseEntity<ResponseObject<List<LayerDto>>> getAllLayers () {
        return getResponseEntity(service.findAllLayer());
    }

    @Operation(summary = "레이어 상세 조회", description = "레이어 상세 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/layer/{layerId}")
    public ResponseEntity<ResponseObject<LayerDto>> getLayerById (@PathVariable(name = "layerId") String layerId) {
        return getResponseEntity(service.findLayerById(layerId));
    }

    @Operation(summary = "레이어 위치 정보조회", description = "특정레이어의 범위 안 위치 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/layer-geom/{layerId}")
    public ResponseEntity<ResponseObject<List<LayerFeatureDto>>> getJsonFeatures (
            @PathVariable(name = "layerId") String layerId,
            @RequestParam(name = "minX", required = false) Double minX,
            @RequestParam(name = "minY", required = false) Double minY,
            @RequestParam(name = "maxX", required = false) Double maxX,
            @RequestParam(name = "maxY", required = false) Double maxY
    ){
        return getResponseEntity(service.findLayerFeaturesByIdAndExtent(layerId, minX, minY, maxX, maxY));
    }

    @Operation(summary = "객체정보상세조회", description = "레이어 객체의 상세 속성정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/layer-feature/{layerId}/{fid}")
    public ResponseEntity<ResponseObject<LayerFeatureDto>> getLayerFeatureById (@PathVariable(name = "layerId") String layerId, @PathVariable(name = "fid") Long fid) {
        return getResponseEntity(service.findLayerFeatureInfo(layerId, fid));
    }

    @Operation(summary = "레이어다운로드", description = "레이어 파일 다운로드")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/layer-download/{layerId}")
    @UncheckedJwtToken
    public void downloadLayer (@PathVariable(name = "layerId") String layerId, HttpServletResponse response) {
        service.downloadLayer(layerId, response);
    }
}
