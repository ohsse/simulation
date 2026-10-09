package com.hscmt.simulation.user.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.jwt.JwtToken;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.simulation.common.annotation.UncheckedJwtToken;
import com.hscmt.simulation.user.dto.*;
import com.hscmt.simulation.user.service.UserService;
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
@Tag(name = "00. 사용자 컨트롤러", description = "사용자 관련 요청 명세")
public class UserController extends CommonController {
    private final UserService userService;

    @Operation(summary = "ID 확인", description = "사용자 ID 존재하는지 확인")
    @ApiResponses(value = {
            @ApiResponse(description = "요청성공", responseCode = "200"),
            @ApiResponse(description = "요청실패", responseCode = "400")
    })
    @GetMapping("/user/exist/{userId}")
    @UncheckedJwtToken
    public ResponseEntity<ResponseObject<Boolean>> existUserById (@PathVariable(name = "userId") String userId) {
        return getResponseEntity(userService.existUserById(userId));
    }

    @Operation(summary = "회원가입", description = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(description = "가입성공", responseCode = "200"),
            @ApiResponse(description = "가입실패", responseCode = "400")
    })
    @PostMapping("/signup")
    @UncheckedJwtToken
    public ResponseEntity<ResponseObject<Void>> signup (@RequestBody SignupDto signupDto) {
        userService.registerUser(signupDto);
        return getResponseEntity();
    }

    @Operation(summary = "로그인", description = "사용자 접속")
    @ApiResponses(value = {
            @ApiResponse(description = "로그인", responseCode = "200"),
            @ApiResponse(description = "로그인 실패", responseCode = "400")
    })
    @PostMapping("/login")
    @UncheckedJwtToken
    public ResponseEntity<ResponseObject<JwtToken>> login (@RequestBody LoginDto loginDto) {
        return getResponseEntity(userService.login(loginDto));
    }
    
    @Operation(summary = "로그아웃", description = "사용자 접속 종료 | 리프레시 토큰 초기화")
    @ApiResponses({
            @ApiResponse(description = "로그아웃", responseCode = "200"),
            @ApiResponse(description = "실패", responseCode = "400")
    })
    @GetMapping("/logout")
    public ResponseEntity<ResponseObject<Void>> logout () {
        userService.logout();
        return getResponseEntity();
    }

    @Operation(summary = "사용자 삭제", description = "사용자 계정 삭제")
    @ApiResponses(value = {
            @ApiResponse(description = "사용자 삭제 성공", responseCode = "200"),
            @ApiResponse(description = "사용자 삭제 실패 사용자 정보 없음", responseCode = "400")
    })
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ResponseObject<Void>> deleteUser (@PathVariable(name = "userId") String userId) {
        userService.deleteUser(userId);
        return getResponseEntity();
    }

    @Operation(summary = "사용자 목록 조회", description = "사용자 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(description = "사용자 목록 조회", responseCode = "200")
    })
    @GetMapping("/users")
    public ResponseEntity<ResponseObject<List<UserDto>>> findAllUser () {
        return getResponseEntity(userService.findAllUser());
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보 수정")
    @ApiResponses(value = {
            @ApiResponse(description = "사용자 정보 수정", responseCode = "200")
    })
    @PutMapping("/user")
    public ResponseEntity<ResponseObject<Void>> updateUser (@RequestBody UserUpsertDto dto) {
        userService.updateUser(dto);
        return getResponseEntity();
    }

    @Operation(summary = "사용자 정보 조회", description = "ID를 통한 사용자 정보 조회")
    @ApiResponses({
            @ApiResponse(description = "사용자 정보 조회", responseCode = "200")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseObject<UserDto>> findUserById (@PathVariable(name = "userId") String userId) {
        return getResponseEntity(userService.findUserById(userId));
    }

    @Operation(summary = "리프레시 토큰 확인", description = "리프레시토큰 확인 후, 유효하면 액세스 토큰 재발급")
    @ApiResponses({
            @ApiResponse(description = "토큰재발급", responseCode = "200"),
            @ApiResponse(description = "재발급실패", responseCode = "400")
    })
    @PostMapping("/user/token/refresh")
    @UncheckedJwtToken
    public ResponseEntity<ResponseObject<JwtToken>> refreshToken (@RequestBody RefreshRequestDto dto) {
        return getResponseEntity(userService.checkRefreshToken(dto));
    }
}
