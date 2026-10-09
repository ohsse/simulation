package com.hscmt.simulation.venv.event;

import java.util.List;

public record VenvCreatedEvent (String venvId, String pyVrsn, List<String> lbrIds) {
}
