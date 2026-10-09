package com.hscmt.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandResult {
    private int exitCode;
    private String outputMessage;
    private String errorMessage;
}
