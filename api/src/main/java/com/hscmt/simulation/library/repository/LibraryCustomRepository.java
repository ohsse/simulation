package com.hscmt.simulation.library.repository;

import com.hscmt.simulation.library.dto.LibraryDto;

import java.util.List;

public interface LibraryCustomRepository {

    void updateModifier(String lbrId, String userId);
    List<LibraryDto> findAllLibraries();
}
