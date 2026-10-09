package com.hscmt.simulation.layer.repository;

import com.hscmt.simulation.layer.domain.LayerList;
import com.hscmt.simulation.layer.key.LayerKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LayerListRepository extends JpaRepository<LayerList, LayerKey>, LayerListCustomRepository  {
}
