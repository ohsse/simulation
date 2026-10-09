package com.hscmt.simulation.library.domain;

import com.hscmt.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lbr_m")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Library extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lbr_id")
    private String lbrId;

    @Column(name = "lbr_nm")
    private String lbrNm;

    @Column(name = "lbr_vrsn")
    private String lbrVrsn;

    @Column(name = "py_vrsn")
    private String pyVrsn;

    @Column(name = "ortx_file_nm")
    private String ortxFileNm;

    public Library (String lbrNm, String lbrVrsn, String pyVrsn, String ortxFileNm) {
        if ( lbrNm != null ) {
            this.lbrNm = lbrNm;
        }

        if ( lbrVrsn != null ) {
            this.lbrVrsn = lbrVrsn;
        }

        if ( pyVrsn != null ){
            this.pyVrsn = pyVrsn;
        }

        if (ortxFileNm != null) {
            this.ortxFileNm = ortxFileNm;
        }
    }
}
