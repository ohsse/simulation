package com.hscmt.simulation.dataset.domain;

import com.hscmt.common.enumeration.CrsyType;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pn_ds_m")
@DiscriminatorValue("PIPE_NETWORK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access =  AccessLevel.PRIVATE)
@Getter
public class PipeNetworkDataset extends Dataset {
    @Column(name = "crsy_type_cd")
    @Enumerated(EnumType.STRING)
    private CrsyType crsyTypeCd;

    /* 생성 */
    public PipeNetworkDataset (PipeNetworkDatasetUpsertDto dto) {
        super(dto);
        this.crsyTypeCd = dto.getCrsyTypeCd();
    }

    /* 수정 */
    public void changeInfo (PipeNetworkDatasetUpsertDto dto) {
        if (dto.getCrsyTypeCd() != null) {
            this.crsyTypeCd = dto.getCrsyTypeCd();
        }
    }
}
