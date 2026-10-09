package com.hscmt.simulation.program.event;

import com.hscmt.common.enumeration.FileExtension;

public record ProgramResultDeletedEvent(String pgmId, String rsltId, String rsltNm, FileExtension fileXtns) {
}
