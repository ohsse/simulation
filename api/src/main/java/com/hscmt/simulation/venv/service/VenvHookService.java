package com.hscmt.simulation.venv.service;

import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.venv.repository.VenvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class VenvHookService {
    private final VenvRepository venvRepository;
    @SimulationTx(propagation = Propagation.REQUIRES_NEW)
    public void createComplete (String venvId) {
        venvRepository.createComplete(venvId);
    }
}
