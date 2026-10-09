package com.hscmt.waternet.lws.service;

import com.hscmt.common.cache.CacheConst;
import com.hscmt.waternet.config.WaternetTx;
import com.hscmt.waternet.lws.dto.LocalCivilApplicantDto;
import com.hscmt.waternet.lws.dto.LocalCustomerUsageDto;
import com.hscmt.waternet.lws.dto.SearchCivilApplicantDto;
import com.hscmt.waternet.lws.dto.SearchLocalUsageDto;
import com.hscmt.waternet.lws.repository.LwsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@WaternetTx(readOnly = true)
public class LwsService {
    private final LwsRepository repository;


    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#p0.searchYyyyMm.toString(), #p0.keyword, #p0.startUsage, #p0.endUsage)"
    )
    public List<LocalCustomerUsageDto> getLocalCustomerUsage (SearchLocalUsageDto searchDto) {
        return repository.getLocalCustomerUsage(searchDto);
    }

    @Cacheable(
            value = CacheConst.CACHE_1DAY,
            key = "T(com.hscmt.common.cache.CacheKeys).generateKey(#p0.startYyyyMmDd.toString(), #p0.endYyyyMmDd.toString(), #p0.keyword)"
    )
    public List<LocalCivilApplicantDto> getLocalCivilApplicants (SearchCivilApplicantDto searchDto) {
        return repository.getLocalCivilApplicants(searchDto);
    }
}
