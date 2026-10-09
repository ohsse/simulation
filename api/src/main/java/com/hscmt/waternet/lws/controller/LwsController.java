package com.hscmt.waternet.lws.controller;

import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.AuthCd;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.waternet.lws.dto.LocalCivilApplicantDto;
import com.hscmt.waternet.lws.dto.LocalCustomerUsageDto;
import com.hscmt.waternet.lws.dto.SearchCivilApplicantDto;
import com.hscmt.waternet.lws.dto.SearchLocalUsageDto;
import com.hscmt.waternet.lws.service.LwsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lws")
@RequiredArgsConstructor
@Tag(name = "98. 지방연계데이터 요청 명세", description = "지방수용가 검침량 | 지방 민원정보 API 명세")
public class LwsController extends CommonController {
    private final LwsService lwsService;

    @Operation(summary = "[지방]수용가검침량 조회", description = "[지방]수용가검침량 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/cstmr-mrdngs")
    @UserRoleCheckRequired(enableAuth = AuthCd.DEVELOPER)
    public ResponseEntity<ResponseObject<List<LocalCustomerUsageDto>>> getLocalCustomerUsage(@ModelAttribute SearchLocalUsageDto searchDto) {
        return getResponseEntity(lwsService.getLocalCustomerUsage(searchDto));
    }

    @Operation(summary = "[지방]민원 조회", description = "[지방]민원정보 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/civil-applicants")
    @UserRoleCheckRequired(enableAuth = AuthCd.DEVELOPER)
    public ResponseEntity<ResponseObject<List<LocalCivilApplicantDto>>> getLocalCivilApplicants (@ModelAttribute SearchCivilApplicantDto searchDto) {
        return getResponseEntity(lwsService.getLocalCivilApplicants(searchDto));
    }
}