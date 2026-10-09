package com.hscmt.waternet.wro.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.waternet.config.WaternetTx;
import com.hscmt.waternet.wro.dto.SearchWideCustomerUsageDto;
import com.hscmt.waternet.wro.dto.WideCustomerUsageDto;
import com.hscmt.waternet.wro.repository.WroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@WaternetTx(readOnly = true)
public class WroService {

    private final WroRepository repository;

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#p0.searchYyyyMm.toString(), #p0.keyword, #p0.startUsage, #p0.endUsage)"
    )
    public List<WideCustomerUsageDto> findAllWideCustomerUsage (SearchWideCustomerUsageDto searchDto) {
        return repository.findAllWideCustomerUsage(searchDto);
    }
}
