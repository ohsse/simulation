package com.hscmt.simulation.program.event;

import java.util.List;

public record ProgramInputFileDeletedEvent(String pgmId, List<String> fileNames) {
}
