package com.hscmt.waternet.tag.domain.child;

import com.hscmt.waternet.tag.domain.RwisData;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "v_rwis_mi_l")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(callSuper = true)
public class RwisMinuteData extends RwisData {
}
