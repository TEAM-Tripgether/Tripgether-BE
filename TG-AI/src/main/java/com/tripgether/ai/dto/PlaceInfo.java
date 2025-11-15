package com.tripgether.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장소 정보 DTO
 * <p>
 * AI 서버에서 추출한 장소 정보를 담는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceInfo {

  @Schema(description = "장소명", example = "명동 교자")
  @NotNull(message = "name은 필수입니다.")
  private String name;

  @Schema(description = "주소", example = "서울특별시 중구 명동길 29")
  @NotNull(message = "address는 필수입니다.")
  private String address;

  @Schema(description = "언어 코드 (ISO 639-1)", example = "ko", allowableValues = {"ko", "en", "ja", "zh"})
  @NotNull(message = "language는 필수입니다.")
  private String language = "ko";

  @Schema(description = "AI 추출 원본 데이터", example = "명동 교자에서 칼국수 먹었어요 (caption, confidence: 0.95)")
  private String rawData;
}
