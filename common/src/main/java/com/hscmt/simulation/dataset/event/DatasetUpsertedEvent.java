package com.hscmt.simulation.dataset.event;

import com.hscmt.common.enumeration.CycleCd;
import com.hscmt.common.enumeration.YesOrNo;

import java.time.LocalDateTime;

public record DatasetUpsertedEvent(String dsId, YesOrNo rltmYn, CycleCd cycleCd, Integer interval, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime rgstDttm) {
}
