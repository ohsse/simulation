package com.hscmt.simulation.program.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.program.dto.ProgramExecuteDto;
import com.hscmt.simulation.program.service.ProgramExecHistService;
import com.hscmt.simulation.program.service.ProgramExecuteAsyncFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "01. 프로그램 제어", description = "프로그램 실행 및 프로그램 중지")
public class ProgramControlController extends CommonController {

    private final ProgramExecuteAsyncFacade asyncFacade;


    @Operation(summary = "프로그램 수동 실행", description = "프로그램 수동 실행 [인수 전달 등]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/program/execute")
    public ResponseEntity<ResponseObject<Void>> executeProgram (@RequestBody ProgramExecuteDto programExecuteDto) {
        asyncFacade.executeProgram(programExecuteDto);
        return getResponseEntity();
    }

    @Operation(summary = "프로그램 강제 종료", description = "프로그램 강제 중지")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/program/terminate/{histId}")
    public ResponseEntity<ResponseObject<Void>> terminateProgram (@PathVariable(name = "histId") String histId) {
        asyncFacade.terminateProgram(histId);
        return getResponseEntity();
    }
}
