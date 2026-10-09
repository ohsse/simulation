package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, String>, DatasetCustomRepository {
}
