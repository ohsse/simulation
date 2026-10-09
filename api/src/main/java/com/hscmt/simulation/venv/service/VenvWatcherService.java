package com.hscmt.simulation.venv.service;

import com.hscmt.common.exception.error.ErrorCode;
import com.hscmt.common.response.SseEvent;
import com.hscmt.simulation.common.comp.SseEmitterComp;
import com.hscmt.simulation.venv.error.VenvErrorCode;
import com.hscmt.simulation.venv.repository.VenvRepository;
import com.hscmt.simulation.venv.domain.VirtualEnvironment;
import com.hscmt.simulation.venv.dto.VenvUpsertResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class VenvWatcherService {

    private final SseEmitterComp emitter;
    private final VenvRepository venvRepository;

    public SseEmitter registerWatcher ( String venvId ) {
        SseEmitter resultEmitter = emitter.register(venvId);

        VirtualEnvironment findVenv = venvRepository.findById(venvId).orElse(null);

        if ( findVenv == null ) {
            notifyError(venvId, null, VenvErrorCode.ENV_NOT_FOUND);
        }

        return resultEmitter;
    }

    public void notifyStatus (String venvId, VenvUpsertResultDto dto, String code) {
        emitter.sendEvent(venvId, SseEvent.<VenvUpsertResultDto>
                builder().event("result").data(dto).code(code).build()
        );
    }

    public void notifyError (String venvId, Exception e) {
        emitter.sendEvent(venvId, SseEvent.<String>builder()
                .event("error").data(e.getMessage())
                .code(e.getClass().getSimpleName()).build());
    }

    public void notifyError (String venvId, VenvUpsertResultDto dto, ErrorCode e) {
        emitter.sendEvent(venvId, SseEvent.<VenvUpsertResultDto>
                builder()
                .event("error").data(dto).code(e.name()).build());
    }

}
