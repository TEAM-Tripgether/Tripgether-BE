package com.tripgether.sns.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.sns.constant.ContentPlatform;
import com.tripgether.sns.constant.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Content extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentPlatform platform;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ContentStatus status = ContentStatus.PENDING;

  @Column(nullable = false, length = 255)
  private String platformUploader;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String caption;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String thumbnailUrl;

  @Lob
  @Column(nullable = false, columnDefinition = "TEXT")
  private String originalUrl;

  @Column(nullable = false, length = 500)
  private String title;

  @Lob
  @Column(columnDefinition = "TEXT")
  private String summary;

  @Column
  private LocalDateTime lastCheckedAt;

  /**
   * Content의 상태를 변경합니다.
   *
   * @param newStatus 새로운 상태
   */
  public void updateStatus(ContentStatus newStatus) {
    this.status = newStatus;
  }

}
