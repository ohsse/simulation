package com.hscmt.simulation.group.domain;

import com.hscmt.simulation.group.dto.GroupUpsertDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pgm_grp_m")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgramGroup extends GroupBase {
    public ProgramGroup (GroupUpsertDto dto) {
        super(dto);
    }
}

