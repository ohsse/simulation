package com.hscmt.simulation.program.service;

import com.hscmt.simulation.program.dto.ProgramExecuteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ProgramExecuteAsyncFacade {
    private final ProgramExecuteService programExecuteService;

    @Async("batchExecutor")
    public CompletableFuture<Void> executeProgram (ProgramExecuteDto programExecuteDto) {
        programExecuteService.executeProgram(programExecuteDto);
        return CompletableFuture.completedFuture(null);
    }

    @Async("batchExecutor")
    public CompletableFuture<Void> terminateProgram (String histId) {
        programExecuteService.terminateProgram(histId);
        return CompletableFuture.completedFuture(null);
    }
}
