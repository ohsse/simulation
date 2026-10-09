package com.hscmt.waternet.wro.controller;

import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.AuthCd;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.waternet.wro.dto.SearchWideCustomerUsageDto;
import com.hscmt.waternet.wro.dto.WideCustomerUsageDto;
import com.hscmt.waternet.wro.service.WroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wro")
@RequiredArgsConstructor
@Tag(name = "96. 광역 연계 정보", description = "광역 수용가 및 검침정보 요청명세 API")
public class WroController extends CommonController {

    private final WroService wroService;

    @Operation(summary = "[광역]수용가 검침량 조회", description = "[광역] 수용가 검침량 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/cstmr-mrdngs")
    @UserRoleCheckRequired(enableAuth = AuthCd.DEVELOPER)
    public ResponseEntity<ResponseObject<List<WideCustomerUsageDto>>> getAllWideCustomerUsage (SearchWideCustomerUsageDto searchDto) {
        return getResponseEntity(wroService.findAllWideCustomerUsage(searchDto));
    }
}
