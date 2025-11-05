package com.tripgether.sns.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.sns.constant.ContentPlatform;
import com.tripgether.common.constant.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Content extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private ContentPlatform platform;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ContentStatus status = ContentStatus.PENDING;

  @Column(length = 255, nullable = true)
  private String platformUploader;

  @Column(length = 1000, nullable = true)
  private String caption;

  @Column(length = 500, nullable = true)
  private String thumbnailUrl;

  @Column(nullable = false, length = 2048, unique = true)
  private String originalUrl;

  @Column(length = 500, nullable = true)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String summary;

  @Column(nullable = true)
  private LocalDateTime lastCheckedAt;

}
