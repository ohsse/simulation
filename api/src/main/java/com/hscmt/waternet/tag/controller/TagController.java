package com.hscmt.waternet.tag.controller;

import com.hscmt.common.controller.CommonController;
import com.hscmt.common.response.ResponseObject;
import com.hscmt.waternet.tag.dto.TagDto;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import com.hscmt.waternet.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@Tag(name = "97. 워터넷 태그관련 명세", description = "워터넷 태그 마스터 정보 및 계측정보 관련 API")
public class TagController extends CommonController {

    private final TagService tagService;
    
    @Operation(summary = "태그 정보 조회", description = "워터넷 태그 마스터 정보 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping("/infos")
    public ResponseEntity<ResponseObject<List<TagDto>>> getTagInfos() {
        return getResponseEntity(tagService.findAllWaternetTagInfos());
    }

    @Operation(summary = "태그 트렌드 조회", description = "워터넷 태그 트렌드 데이터 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공")
    })
    @PostMapping("/trend")
    public ResponseEntity<ResponseObject<List<Map<String, Object>>>> getWaternetTrend(@RequestBody TrendSearchDto searchDto) {
        return getResponseEntity(tagService.getWaternetTagTrendData(searchDto));
    }
}
