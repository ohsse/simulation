package com.hscmt.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SseEvent <T>{
    private final String event;
    private final T data;
    private final String code;
}
