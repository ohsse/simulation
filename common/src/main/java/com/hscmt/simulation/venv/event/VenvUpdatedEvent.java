package com.hscmt.simulation.venv.event;

import java.util.List;

public record VenvUpdatedEvent(String venvId, List<String> addLbrIds, List<String> delLbrNms) {
}
