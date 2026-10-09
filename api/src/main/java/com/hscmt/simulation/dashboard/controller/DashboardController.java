package com.hscmt.simulation.dashboard.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.dashboard.dto.DashboardDto;
import com.hscmt.simulation.dashboard.dto.DashboardUpsertDto;
import com.hscmt.simulation.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "09. 대시보드", description = "대시보드 관련 요청 명세")
public class DashboardController extends CommonController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 수정", description = "대시보드 신규 및 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200" , description = "성공")
    })
    @PostMapping("/dsbd")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> upsert (@RequestBody DashboardUpsertDto dto) {
        dashboardService.upsert(dto);
        return getResponseEntity();
    }

    @Operation(summary = "대시보드 삭제", description = "대시보드 단건 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @DeleteMapping("/dsbd/{dsbdId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> delete (@PathVariable(name = "dsbdId") String dsbdId) {
        dashboardService.delete(dsbdId);
        return getResponseEntity();
    }

    @Operation(summary = "대시보드 조회", description = "대시보드 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/dsbds")
    public ResponseEntity<ResponseObject<List<DashboardDto>>> findAllDashboards () {
        return getResponseEntity(dashboardService.findAllDashboards(null));
    }

    @Operation(summary = "대시보드 상세 조회", description = "대시보드 상세 조회 ById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/dsbd/{dsbdId}")
    public ResponseEntity<ResponseObject<DashboardDto>> findDashboardById (@PathVariable(name = "dsbdId") String dsbdId) {
        return getResponseEntity(dashboardService.findDashboard(dsbdId));
    }
}