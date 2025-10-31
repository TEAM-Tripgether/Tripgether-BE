package com.tripgether.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 서버로 장소 추출 요청 시 사용하는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceExtractionRequest {

  @Schema(description = "Content UUID", example = "550e8400-e29b-41d4-a716-446655440000")
  @NotNull(message = "contentId는 필수입니다.")
  private UUID contentId;

  @Schema(description = "SNS URL", example = "https://www.instagram.com/p/ABC123/")
  @NotNull(message = "snsUrl은 필수입니다.")
  private String snsUrl;
}
