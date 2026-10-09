package com.hscmt.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public class BaseEntity {
    @CreatedBy
    @Column(name = "rgst_id", updatable = false, nullable = false)
    private String rgstId;

    @CreatedDate
    @Column(name = "rgst_dttm", updatable = false, nullable = false)
    private LocalDateTime rgstDttm;

    @CreatedBy
    @LastModifiedBy
    @Column(name = "mdf_id", nullable = false)
    private String mdfId;

    @CreatedDate
    @LastModifiedDate
    @Column(name = "mdf_dttm", nullable = false)
    private LocalDateTime mdfDttm;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.rgstDttm = now;
        this.mdfDttm = now;
    }

    @PreUpdate
    public void preUpdate() {
        LocalDateTime now = LocalDateTime.now();
        this.mdfDttm = now;
        this.rgstDttm = now;
    }

    public String getRgstId() {
        return this.rgstId;
    }

    public LocalDateTime getRgstDttm() {
        return this.rgstDttm;
    }

    public String getMdfId() {
        return this.mdfId;
    }

    public LocalDateTime getMdfDttm() {
        return this.mdfDttm;
    }
}
