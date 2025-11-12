package com.tripgether.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * AI 서버로부터 Webhook Callback으로 받는 분석 결과 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackRequest {

  @Schema(description = "Content UUID")
  @NotNull(message = "contentId는 필수입니다.")
  private UUID contentId;

  @Schema(description = "처리 결과 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"})
  @NotNull(message = "resultStatus는 필수입니다.")
  private String resultStatus;

  @Schema(description = "SNS 플랫폼", example = "INSTAGRAM", allowableValues = {"INSTAGRAM", "YOUTUBE_SHORTS"})
  @NotNull(message = "snsPlatform은 필수입니다.")
  private String snsPlatform;

  @Schema(description = "콘텐츠 정보")
  @Valid
  private ContentInfo contentInfo;

  @Schema(description = "추출된 장소 목록")
  @Valid
  private List<PlaceInfo> places;

  /**
   * 콘텐츠 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ContentInfo {

    @Schema(description = "콘텐츠 제목", example = "서울 여행 브이로그")
    @NotNull(message = "title은 필수입니다.")
    private String title;

    @Schema(description = "콘텐츠 URL", example = "https://instagram.com/v/t51.2885-15/234123")
    @NotNull(message = "contentUrl은 필수입니다.")
    private String contentUrl;

    @Schema(description = "썸네일 URL", example = "https://cdn.instagram.com/v/t51.2885-15/...")
    @NotNull(message = "thumbnailUrl은 필수입니다.")
    private String thumbnailUrl;

    @Schema(description = "업로더 아이디", example = "travel_lover")
    @NotNull(message = "platformUploader는 필수입니다.")
    private String platformUploader;
  }

  /**
   * 장소 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PlaceInfo {

    @Schema(description = "장소명", example = "명동 교자")
    @NotNull(message = "name은 필수입니다.")
    private String name;

    @Schema(description = "주소", example = "서울특별시 중구 명동길 29")
    @NotNull(message = "address는 필수입니다.")
    private String address;

    @Schema(description = "언어 코드 (ISO 639-1)", example = "ko", allowableValues = {"ko", "en", "ja", "zh"})
    @NotNull(message = "language는 필수입니다.")
    private String language;

    @Schema(description = "AI 추출 원본 데이터", example = "명동 교자에서 칼국수 먹었어요 (caption, confidence: 0.95)")
    private String rawData;
  }
}
