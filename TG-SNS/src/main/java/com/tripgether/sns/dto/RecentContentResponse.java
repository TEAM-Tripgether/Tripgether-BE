package com.tripgether.sns.dto;

import com.tripgether.common.constant.ContentStatus;
import com.tripgether.sns.constant.ContentPlatform;
import com.tripgether.sns.entity.Content;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentContentResponse {
  private UUID contentId;
  private ContentPlatform platform;      // 예: YOUTUBE, INSTAGRAM
  private String title;
  private String thumbnailUrl;
  private String originalUrl;
  private ContentStatus status; // PENDING / COMPLETED / FAILED 등
  private LocalDateTime createdAt;

  public static RecentContentResponse fromEntity(Content content) {
    return RecentContentResponse.builder()
        .contentId(content.getId())
        .platform(content.getPlatform())
        .title(content.getTitle())
        .thumbnailUrl(content.getThumbnailUrl())
        .originalUrl(content.getOriginalUrl())
        .status(content.getStatus())
        .createdAt(content.getCreatedAt())
        .build();
  }
}
