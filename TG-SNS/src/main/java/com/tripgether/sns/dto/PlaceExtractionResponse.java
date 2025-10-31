package com.tripgether.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 서버로부터 장소 추출 요청 응답 (202 Accepted)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceExtractionResponse {

  @Schema(description = "Content UUID")
  private UUID contentId;

  @Schema(description = "처리 상태", example = "ACCEPTED")
  private String status;
}
