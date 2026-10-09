package com.hscmt.waternet.tag.service;

import com.hscmt.waternet.config.WaternetTx;
import com.hscmt.waternet.tag.dto.TagDto;
import com.hscmt.waternet.tag.dto.TrendSearchDto;
import com.hscmt.waternet.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@WaternetTx(readOnly = true)
public class TagService {
    private final TagRepository repository;

    /* 워터넷 태그 조회 */
    public List<TagDto> findAllWaternetTagInfos () {
        return repository.findAllWaternetTags();
    }

    @Cacheable(
            value = "cache1min",
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey('waternetTrend',#dto.cycleCd, #dto.searchStrtDttm, #dto.searchEndDttm, #dto.tagList)"
    )
    /* 워터넷 태그 트렌드 조회 */
    public List<Map<String, Object>> getWaternetTagTrendData (TrendSearchDto dto) {
        return repository.getWaternetTrendData (dto);
    }
}
