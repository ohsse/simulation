package com.hscmt.simulation.group.domain;

import com.hscmt.simulation.group.dto.GroupUpsertDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dsbd_grp_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DashboardGroup extends GroupBase {
    public DashboardGroup (GroupUpsertDto dto) {
        super(dto);
    }


}
