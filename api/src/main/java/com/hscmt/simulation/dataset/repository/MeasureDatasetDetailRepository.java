package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.domain.MeasureDatasetDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasureDatasetDetailRepository extends JpaRepository<MeasureDatasetDetail, String>, MeasureDatasetDetailCustomRepository {
}
