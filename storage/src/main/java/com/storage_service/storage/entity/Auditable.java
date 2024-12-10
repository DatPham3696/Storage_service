package com.storage_service.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@MappedSuperclass
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auditable {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected Instant createdDate = Instant.now().plus(7, ChronoUnit.HOURS);
    @LastModifiedDate
    @Column(name = "updated_at")
    protected Instant lastModifiedDate = Instant.now().plus(7, ChronoUnit.HOURS);
    @CreatedBy
    @Column(name = "created_by")
    protected String createdBy;
    @LastModifiedBy
    @Column(name = "last_modified_by")
    protected String lastModifiedBy;
}
