package com.hscmt.simulation.program.event;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;

import java.time.LocalDateTime;

public record ProgramUpsertedEvent (String pgmId, LocalDateTime strtExecDttm, CycleCd cycleCd, Integer interval, YesOrNo rltmYn) {
}
