package com.hscmt.common.controller;

import com.hscmt.common.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public class CommonController {

    /* 데이터 포함 리턴 */
    protected <T> ResponseEntity<ResponseObject<T>> getResponseEntity (T data) {
        return ResponseEntity.ok(
                ResponseObject.<T>builder()
                        .data(data)
                        .code("SUCCESS")
                        .build()
        );
    }

    /* 데이터 없이 리턴 */
    protected <Void> ResponseEntity<ResponseObject<Void>> getResponseEntity () {
        return ResponseEntity.ok(
            ResponseObject.<Void>builder()
                    .code("SUCCESS")
                    .build()
        );
    }
}
