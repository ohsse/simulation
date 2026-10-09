package com.hscmt.simulation.dataset.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.dto.FromToSearchDto;
import com.hscmt.common.enumeration.FileExtension;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetTrendDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureSearchDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkJsonResponse;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkUrlResponse;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkVisResponse;
import com.hscmt.simulation.dataset.service.DatasetViewService;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dataset-vis")
@Tag(name = "05. 데이터셋 시각화 관련 요청 명세", description = "계측 및 관망데이터셋 시각화 관련")
public class DatasetViewController extends CommonController {

    private final DatasetViewService datasetViewService;
    
    @Operation(summary = "계측데이터셋 데이터 조회", description = "계측데이터셋 계측데이터 조회")
    @ApiResponses({
            @ApiResponse(description = "계측데이터셋 데이터 조회", responseCode = "200")
    })
    @GetMapping("/measure/{dsId}")
    public ResponseEntity<ResponseObject<MeasureDatasetTrendDto>> getMeasureDatasetData(@PathVariable(name = "dsId") String dsId, @ModelAttribute MeasureSearchDto searchDto) {
        return getResponseEntity(datasetViewService.getMeasureDataset(dsId, searchDto));
    }

    @Operation(summary = "관망데이터셋 조회", description = "확장자 INP|SHP -> Json, TIFF -> 경로 반환")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(schema = @Schema(oneOf = {PipeNetworkJsonResponse.class, PipeNetworkUrlResponse.class}))
            )
    })
    @GetMapping("/pipe/{dsId}")
    public ResponseEntity<ResponseObject<PipeNetworkVisResponse>> getPipeNetworkDatasetVisData (@PathVariable(name = "dsId") String dsId, @RequestParam(name = "fileXtns") FileExtension fileExtension) {
        return getResponseEntity(datasetViewService.getPipeNetworkDataset(dsId, fileExtension));
    }
}
