package com.hscmt.simulation.venv.domain;

import com.hscmt.common.domain.DomainEventEntity;
import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.simulation.venv.dto.VenvCreateDto;
import com.hscmt.simulation.venv.dto.VenvUpdateDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venv_m")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VirtualEnvironment extends DomainEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "venv_id")
    private String venvId;

    @Column(name = "venv_nm")
    private String venvNm;

    @Column(name = "venv_desc")
    private String venvDesc;

    @Column(name = "py_vrsn")
    private String pyVrsn;

    @Column(name = "use_able_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo useAbleYn;

    public VirtualEnvironment (VenvCreateDto dto) {
        this.venvNm = dto.getVenvNm();
        this.venvDesc = dto.getVenvDesc();
        this.pyVrsn = dto.getPyVrsn();
        this.useAbleYn = YesOrNo.N;
    }

    public void update (VenvUpdateDto dto) {
        if (dto.getVenvNm() != null) {
            this.venvNm = dto.getVenvNm();
        }
        if(dto.getVenvDesc() != null) {
            this.venvDesc = dto.getVenvDesc();
        }
    }
}
