package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.domain.MeasureDataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasureDatasetRepository extends JpaRepository<MeasureDataset, String>, MeasureDatasetCustomRepository {
}
