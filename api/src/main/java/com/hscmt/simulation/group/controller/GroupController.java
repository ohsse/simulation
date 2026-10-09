package com.hscmt.simulation.group.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.enumeration.GroupType;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.common.aop.UserRoleCheckRequired;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.service.GroupServiceFactory;
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
@Tag(name = "99. 그룹관련 요청 명세", description = "프로그램|데이터셋|대시보드 그룹관련 요청명세")
public class GroupController extends CommonController {

    private final GroupServiceFactory factory;

    @Operation(summary = "그룹추가", description = "그룹추가 [단건]")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping("/group/{groupType}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<String>> createGroup(@PathVariable(name = "groupType") GroupType groupType, @RequestBody GroupUpsertDto group) {
        factory.getService(groupType).upsertGroup(group);
        return getResponseEntity();
    }

    @Operation(summary = "그룹 추가 수정", description = "그룹 목록 추가 | 수정 [다건]")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping("/groups/{groupType}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> createGroups (@PathVariable(name = "groupType") GroupType groupType, @RequestBody List<GroupUpsertDto> groups) {
        factory.getService(groupType).upsertGroups(groups);
        return getResponseEntity();
    }

    @Operation(summary = "그룹 삭제", description = "그룹 삭제[다건]")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @PostMapping("/groups-del/{groupType}")
    @UserRoleCheckRequired
    public ResponseEntity<ResponseObject<Void>> deleteGroups (@RequestBody List<String> groupIds, @PathVariable(name = "groupType") GroupType groupType) {
        factory.getService(groupType).deleteGroups(groupIds);
        return getResponseEntity();
    }

    @Operation(summary = "그룹 목록 조회", description = "그룹 목록 조회")
    @ApiResponses({
            @ApiResponse(description = "성공", responseCode = "200")
    })
    @GetMapping("/groups/{groupType}")
    public ResponseEntity<ResponseObject<GroupDto>> findAllGroups (@PathVariable(name = "groupType") GroupType groupType) {
        return getResponseEntity(factory.getService(groupType).getGroupsWithChildrenAndItems(null));
    }
}
