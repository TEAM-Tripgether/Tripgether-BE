package com.tripgether.be.global.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // TODO: 삭제자 정보는 추후 User 엔티티와 연동 필요
    @Column(name = "deleted_by")
    private String deletedBy;

    // === 메소드 ===

    /**
     * 소프트 삭제 처리
     *
     * @param deletedBy 삭제한 사용자 ID 또는 식별자
     */
    public void softDelete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 소프트 삭제 복구
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * 삭제 여부 확인
     *
     * @return 삭제된 경우 true, 아니면 false
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted;
    }

    /**
     * 활성 상태 확인 (삭제되지 않은 상태)
     *
     * @return 활성 상태면 true, 삭제된 상태면 false
     */
    public boolean isActive() {
        return !isDeleted();
    }
}
