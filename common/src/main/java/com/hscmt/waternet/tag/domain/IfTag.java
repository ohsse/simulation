package com.hscmt.waternet.tag.domain;

import com.hscmt.common.enumeration.YesOrNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "if_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class IfTag {
    @Id
    @Column(name = "tag_sn")
    private String tagSn;
    @Column(name = "tag_se_cd")
    private String tagSeCd;
    @Column(name = "tag_desc")
    private String tagDesc;
    @Column(name = "tag_alias")
    private String tagAlias;
    @Column(name = "use_yn")
    @Enumerated(EnumType.STRING)
    private YesOrNo useYn;
}
