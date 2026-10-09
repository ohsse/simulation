package com.hscmt.simulation.venv.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.venv.dto.*;
import com.hscmt.simulation.venv.service.VenvService;
import com.hscmt.simulation.venv.service.VenvWatcherService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "02. 가상환경 관련 요청 명세", description = "가상환경 추가|수정|삭제")
public class VenvController extends CommonController {
    private final VenvService venvService;
    private final VenvWatcherService watchService;

    @Operation(summary = "가상환경 생성", description = "아나콘다 가상환경 생성 및 패키지 등록")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping("/venv")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<String>> createVenv (@RequestBody VenvCreateDto createDto) {
        return getResponseEntity(venvService.createVirtualEnvironment(createDto));
    }

    @Operation(summary = "가상환경 수정", description = "가상환경 기본정보 수정 및 패키지 등록|삭제")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PutMapping("/venv")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<String>> updateVenv (@RequestBody VenvUpdateDto updateDto) {
        return getResponseEntity(venvService.updateVirtualEnvironment(updateDto));
    }

    /**
     * SseEmitter는 HTTP 스트림을 직접 관리하는 클래스
     * SseEmitter는 응답 본문(JSON 등)을 한 번에 내려주는 게 아니라,
     * HTTP 연결을 유지한 채로 서버에서 데이터를 여러 번, 지연되게 보내는 방식
     * 즉, SseEmitter는 "이제부터 내가 이 응답 스트림 관리할게!" 라고 선언하는 특수한 타입
     */
    @Operation(summary = "가상환경 적용 알람", description = "가상환경 및 가상환경의 적용할 패키지 완료 알람")
    @ApiResponses(value = {
            @ApiResponse(description = "응답완료", responseCode = "200", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VenvUpsertResultDto.class))
            }),
    })
    @GetMapping(value = "/venv/await/{venvId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @UncheckedJwtToken
    public SseEmitter awaitStatus (@PathVariable(name = "venvId") String venvId) {
        return watchService.registerWatcher(venvId);
    }

    @Operation(summary = "가상환경 삭제", description = "가상환경 삭제")
    @ApiResponses(value = {
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @DeleteMapping("/venv/{venvId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<String>> deleteVenv (@PathVariable(name = "venvId") String venvId) {
        return getResponseEntity(venvService.deleteVirtualEnvironment(venvId));
    }

    @Operation(summary = "가상환경 목록 조회", description = "가상환경 목록 조회")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/venvs")
    public ResponseEntity<ResponseObject<List<VenvDto>>> getVenvs (@ModelAttribute(name = "pyVrsn") String pyVrsn) {
        return getResponseEntity(venvService.findAllVenvs(pyVrsn));
    }

    @Operation(summary = "가상환경 상세 조회", description = "가상환경의 기본정보와 가상환경에 적용된 파이썬 패키지 목록 도출")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/venv/{venvId}")
    public ResponseEntity<ResponseObject<VenvDto>> findVenvInfoById (@PathVariable(name = "venvId") String venvId) {
        return getResponseEntity(venvService.findVenvInfoWithLibrary (venvId));
    }

    @Operation(summary = "가상환경 패키지 디렉토리 조회", description = "가상환경에 적용된 파이썬 패키지 구조 조회 [실제경로 없애고 가상경로로 반환]")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/venv/package/{venvId}")
    public ResponseEntity<ResponseObject<VenvPackageDto>> findVenvPyPackages (@PathVariable(name = "venvId") String venvId) {
        return getResponseEntity(venvService.findVenvAllPyPackageFiles(venvId));
    }

    @Operation(summary = "가상환경 패키지 파일 업로드", description = "가상환경 패키지 폴더 특정경로에 파일을 업로드 한다.")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping(value = "/venv/package/{venvId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> uploadPackageFiles (
            @PathVariable(name = "venvId") String venvId,
            @RequestPart(name = "targetPath") String targetPath,
            @RequestPart(name = "files") List<MultipartFile> files
    ) {
        venvService.uploadFilesInPackageDirectory(venvId, targetPath, files);
        return getResponseEntity();
    }

    @Operation(summary = "가상환경 패키지 파일 삭제", description = "가상환경 패키지 파일 삭제")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @DeleteMapping(value = "/venv/package/{venvId}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deletePackageFile (@PathVariable(name = "venvId") String venvId, @ModelAttribute(name = "filePath") String filePath) {
        venvService.deletePackageFile(venvId, filePath);
        return getResponseEntity();
    }
}
