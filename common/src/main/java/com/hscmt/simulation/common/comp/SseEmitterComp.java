package com.hscmt.simulation.common.comp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hscmt.common.response.SseEvent;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * emitter 공통 컴포넌트
 */
@Component
public class SseEmitterComp {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* emitter 등록 */
    public SseEmitter register (String key) {
        return register (key, Long.MAX_VALUE);
    }

    /* emitter 등록 */
    public SseEmitter register (String key, Long timeout) {
        if (timeout == null) {
            timeout = Long.MAX_VALUE;
        }
        SseEmitter emitter = new SseEmitter(timeout);
        emitters.put(key, emitter);

        emitter.onCompletion(() -> removeWatcher(key));
        emitter.onTimeout(() -> removeWatcher(key));
        emitter.onError((error) -> removeWatcher(key));
        return emitter;
    }
    
    /* emitter 이벤트 전송 */
    public <T> void sendEvent (String key, SseEvent<T> event) {
        SseEmitter emitter = emitters.get(key);
        if (emitter != null) {
            try {
                String json = objectMapper.writeValueAsString(event);
                emitter.send(json, MediaType.APPLICATION_JSON);
                emitter.complete();
            } catch ( Exception e ) {
                emitter.completeWithError(e);
            } finally {
                removeWatcher(key);
            }
        }
    }

    protected void removeWatcher (String key) {
        emitters.remove(key);
    }
}
