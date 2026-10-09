package com.hscmt.waternet.tag.service;

import com.hscmt.simulation.collect.dto.TagCollectDto;
import com.hscmt.waternet.tag.dto.TagDataDto;
import com.hscmt.waternet.tag.dto.TagDto;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import com.hscmt.waternet.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository repository;

    /* 워터넷 태그 조회 */
    public List<TagDto> findAllWaternetTagInfos () {
        return repository.findAllWaternetTags();
    }

    /* 워터넷 태그 트렌드 조회 */
    public List<Map<String, Object>> getWaternetTagTrendData (TrendSearchDto dto) {
        return repository.getWaternetTrendData (dto);
    }

    public TagDataDto findTagData (TagCollectDto dto) {
        return repository.findTagData(dto);
    }

    public List<TagDataDto> findTagDataList (TagCollectDto dto) {
        return repository.findTagDataList(dto);
    }
}
