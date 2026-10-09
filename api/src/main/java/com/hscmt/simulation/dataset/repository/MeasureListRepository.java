package com.hscmt.simulation.dataset.repository;

import com.hscmt.simulation.dataset.domain.MeasureList;
import com.hscmt.simulation.dataset.key.MeasureListKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasureListRepository extends JpaRepository<MeasureList, MeasureListKey>, MeasureListCustomRepository {
}
