package com.hscmt.simulation.library.repository;

import com.hscmt.simulation.library.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, String>, LibraryCustomRepository {

    Library findByOrtxFileNm(String ortxFileNm);
}
