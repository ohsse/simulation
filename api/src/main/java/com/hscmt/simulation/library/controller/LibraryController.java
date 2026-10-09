package com.hscmt.simulation.library.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.library.dto.LibraryDto;
import com.hscmt.simulation.library.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "01. 파이썬 패키지 관련 요청 명세", description = "파이썬 라이브러리 패키지 등록|삭제|일부파일 변경 ")
public class LibraryController extends CommonController {
    private final LibraryService libraryService;

    @Operation(summary = "패키지 등록", description = "파이썬 라이브러리 패키지 등록")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200"),
            @ApiResponse(description = "업로드실패", responseCode = "400"),
    })
    @PostMapping(value = "/lbrs/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} , produces = {MediaType.APPLICATION_JSON_VALUE})
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> uploadLibraries (
            @Parameter(
                    name = "files",
                    description = "라이브러리 패키지 파일 목록",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MultipartFile.class, type = "string", format = "binary")))
            )
            @RequestPart(name = "files") List<MultipartFile> files
    ) {
        libraryService.registerPythonLibraries(files);
        return getResponseEntity();
    }

    @Operation(summary = "패키지 단건 삭제", description = "파이썬 라이브러리 패키지 단건 삭제")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @DeleteMapping("/lbr/{lbrId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deleteLibrary (@PathVariable(name = "lbrId") String lbrId) {
        libraryService.deleteLibrary(lbrId);
        return getResponseEntity();
    }

    @Operation(summary = "패키지 다건 삭제", description = "파이썬 라이브러리 패키지 다건 삭제")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping("/lbrs/remove")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deleteLibraries (@RequestBody List<String> lbrIds) {
        libraryService.deleteLibraries(lbrIds);
        return getResponseEntity();
    }

    @Operation(summary = "패키지 목록 조회", description = "패키지 목록 조회")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/lbrs")
    public ResponseEntity<ResponseObject<List<LibraryDto>>> findAllPyLibraries (@RequestParam(name = "pyVrsn", required = false) String pyVrsn){
        return getResponseEntity(libraryService.findAllPyLibraries(pyVrsn));
    }
}
