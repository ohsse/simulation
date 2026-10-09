package com.hscmt.simulation.group.repository;

import com.hscmt.simulation.group.domain.GroupBase;
import com.hscmt.simulation.group.dto.GroupDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBaseRepository <E extends GroupBase> extends JpaRepository<E, String > {

}


