package com.hscmt.simulation.job;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.JobTargetType;

import java.time.LocalDateTime;

public record ScheduleJobRequest(
        JobTargetType group,
        String targetId,
        LocalDateTime firstExecDttm,
        CycleCd cycleCd,
        Integer interval
)
{
}
