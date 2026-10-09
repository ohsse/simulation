package com.hscmt.simulation.program.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.DirectionType;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.program.domain.ProgramExecHist;
import com.hscmt.simulation.program.dto.*;
import com.hscmt.simulation.program.dto.vis.VisualizationItem;
import com.hscmt.simulation.program.service.ProgramVisualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "08. 프로그램 시각화", description = "프로그램 시각화 관련 요청 명세")
public class ProgramVisualizationController extends CommonController {

    private final ProgramVisualizationService service;

    @Operation(summary = "프로그램시각화설정항목조회", description = "프로그램 시각화 설정 가능한 항목들 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/vis/items/{pgmId}")
    public ResponseEntity<ResponseObject<List<? extends ProgramVisualizationItemDto>>> getProgramVisualizationItems(@PathVariable(name = "pgmId") String pgmId) {
        return getResponseEntity(service.getProgramVisualizationItems(pgmId));
    }

    @Operation(summary = "프로그램 시각화 목록 조회", description = "프로그램에 설정한 프로그램 시각화 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/vis/{pgmId}")
    public ResponseEntity<ResponseObject<List<ProgramVisualizationDto>>> getProgramVisualizationList(@PathVariable(name = "pgmId") String pgmId) {
        return getResponseEntity(service.findAllProgramVisualizations(pgmId));
    }

    @Operation(summary = "프로그램시각화데이터조회", description = "프로그램 시각화 데이터 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/vis")
    public ResponseEntity<ResponseObject<VisualizationItem>> getProgramVisualizationDisplay(@RequestParam(name = "visId") String visId, @RequestParam(name = "histId") String histId) {
        return getResponseEntity(service.pgmResultToVisData(visId, histId));
    }

    @Operation(summary = "이력ID 조회(By방향)", description = "특정 시각화의 이력ID 조회 특정 이력ID를 기준으로 이전, 이후 ID 탐색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/vis/hist")
    public ResponseEntity<ResponseObject<ProgramExecHistDto>> getPgmVisHistIdByDirection(@ModelAttribute PgmVisSearchDto dto) {
        return getResponseEntity(service.getPgmVisHistIdByDirection(dto));
    }

    @Operation(summary = "이력 ID 조회(By시간)", description = "특정 시간을 입력하였을 때, 그 시간에 가까운 전후이력 3~5개를 탐색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/vis/hist/{visId}")
    public ResponseEntity<ResponseObject<List<ProgramExecHistDto>>> getPgmVisHistIdByTime(@PathVariable(name = "visId") String visId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime execStrtDttm) {
        return getResponseEntity(service.getPgmVisHitIdByTime(visId, execStrtDttm));
    }
}
