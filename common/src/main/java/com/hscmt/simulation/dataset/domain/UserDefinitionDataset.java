package com.hscmt.simulation.dataset.domain;

import com.hscmt.simulation.dataset.dto.ud.UserDefinitionDatasetUpsertDto;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* 사용자 정의 데이터셋 아직 특별한 컬럼 정의는 없다. */
@Entity
@Table(name = "udf_ds_m")
@DiscriminatorValue("USER_DEF")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDefinitionDataset extends Dataset{

    public UserDefinitionDataset (UserDefinitionDatasetUpsertDto dto) {
        super(dto);
    }
}
