package com.hscmt.simulation.os.controller;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.os.dto.OperationSystemInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "95. 서버상태", description = "서버상태 조회")
public class OperationInfoController extends CommonController {
    @Operation(summary = "OS 상태 조회", description = "메모리 및 CPU 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/os/status")
    @Cacheable(
            value = CacheConst.CACHE_1SEC,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(T(com.hscmt.common.cache.CacheConst).OS_INFO)"
    )
    public ResponseEntity<ResponseObject<OperationSystemInfoDto>> getOperationSystemInfo () {
        return getResponseEntity(new OperationSystemInfoDto());
    }
}
