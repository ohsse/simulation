package com.hscmt.simulation.os.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hscmt.common.util.OperationSystemUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "운영시스템정보")
@Data
public class OperationSystemInfoDto {
    @Schema(description = "전체메모리")
    private String totalMemory;
    @Schema(description = "여유메모리")
    private String freeMemory;
    @Schema(description = "사용메모리")
    private String usedMemory;
    @Schema(description = "메모리사용율")
    private String memoryUsageRate;
    @Schema(description = "CPU이용율")
    private String cpuUsageRate;
    @Schema(description = "발생시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    public OperationSystemInfoDto() {
        this.totalMemory = OperationSystemUtil.getTotalMemory();
        this.freeMemory = OperationSystemUtil.getAvailableMemory();
        this.usedMemory = OperationSystemUtil.getUsedMemory();
        this.memoryUsageRate = OperationSystemUtil.getMemoryUseRate();
        this.cpuUsageRate = OperationSystemUtil.getCpuUseRate();
        this.occurTime = LocalDateTime.now();
    }
}
