package com.tripgether.application.entity;

import com.tripgether.application.constant.AiJobStatus;
import com.tripgether.application.constant.AiJobType;
import com.tripgether.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tripgether.sns.entity.Content;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @jakarta.persistence.Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AiJobStatus status = AiJobStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int attempt = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxAttempt = 3;

    @Lob
    @Column(columnDefinition = "JSONB")
    private String result;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null)
            status = AiJobStatus.PENDING;
        if (maxAttempt <= 0)
            maxAttempt = 3;
    }

}
