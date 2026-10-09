package com.hscmt.simulation.program.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.DownloadType;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.simulation.program.dto.ProgramExecHistDto;
import com.hscmt.simulation.program.dto.ProgramExecSearchDto;
import com.hscmt.simulation.program.dto.ProgramFileDto;
import com.hscmt.simulation.program.service.ProgramExecHistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "06. 프로그램 이력", description = "프로그램 개정이력, 프로그램 실행이력 관련 요청명세")
public class ProgramHistController extends CommonController {

    private final ProgramExecHistService programExecHistService;


    @Operation(summary = "프로그램 실행이력 조회", description = "프로그램 실행이력 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/exec/hists")
    public ResponseEntity<ResponseObject<List<ProgramExecHistDto>>> findAllProgramExecHist (@ModelAttribute ProgramExecSearchDto searchDto) {
        return getResponseEntity(programExecHistService.findAllProgramExecHistList(searchDto));
    }

    @Operation(summary = "프로그램 개정이력 조회", description = "프로그램 파일 변경이력 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/pgm/hists/{pgmId}")
    @UncheckedJwtToken
    public ResponseEntity<ResponseObject<List<ProgramFileDto>>> findAllProgramFileHist (@PathVariable(name = "pgmId") String pgmId ) {
        return getResponseEntity(programExecHistService.findAllProgramFileHist(pgmId));
    }

    @Operation(summary = "프로그램관련 다운로드", description = "프로그램 개정이력 or 프로그램 실행결과 다운로드")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/pgm/download/{downloadType}/{pgmId}")
    @UncheckedJwtToken
    public void downloadProgramFiles (@PathVariable(name = "downloadType") DownloadType downloadType, @PathVariable(name = "pgmId") String pgmId, @RequestBody List<String> dirIdList, HttpServletResponse response) {
        programExecHistService.downloadFiles(downloadType, pgmId, dirIdList, response);
    }

}
