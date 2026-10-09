package com.hscmt.simulation.layer.service;

import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.layer.repository.LayerListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class LayerManageService {
    private final LayerListRepository repository;

    /* 레이어 내역 전체 삭제 */
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByLayerId(String layerId) {
        repository.deleteAllByLayerId(layerId);
    }
}
