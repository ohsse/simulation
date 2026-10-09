package com.hscmt.simulation.program.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.program.dto.ProgramDto;
import com.hscmt.simulation.program.dto.ProgramUpsertDto;
import com.hscmt.simulation.program.service.ProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "04. 프로그램 관련 요청 명세", description = "프로그램 등록|수정|삭제")
public class ProgramController extends CommonController {

    private final ProgramService programService;

    @Operation(summary = "프로그램 추가|수정", description = "프로그램 등록 및 프로그램 파일 등록")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping(value = "/pgm", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> saveProgram (
            @RequestPart(name = "info") ProgramUpsertDto info,
            @RequestPart(name = "file", required = false) MultipartFile file) {
        programService.save(info, file);
        return getResponseEntity();
    }

    @Operation(summary = "프로그램 삭제", description = "프로그램 삭제")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @DeleteMapping("/pgm/{pgmId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deleteProgram (@PathVariable(name = "pgmId") String pgmId) {
        programService.delete(pgmId);
        return getResponseEntity();
    }

    @Operation(summary = "프로그램 목록 조회", description = "프로그램 목록 조회")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/pgms")
    public ResponseEntity<ResponseObject<List<ProgramDto>>> findAllPrograms () {
        return getResponseEntity(programService.findAllPrograms(null));
    }

    @Operation(summary = "프로그램 조회", description = "프로그램 상세 조회")
    @ApiResponses({
            @ApiResponse(description = "성공" ,responseCode = "200")
    })
    @GetMapping("/pgm/{pgmId}")
    public ResponseEntity<ResponseObject<ProgramDto>> findProgram(@PathVariable(name = "pgmId") String pgmId) {
        return getResponseEntity(programService.findProgram(pgmId));
    }
}