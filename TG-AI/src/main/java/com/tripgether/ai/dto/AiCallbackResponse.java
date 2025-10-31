package com.tripgether.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 서버 Webhook Callback에 대한 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackResponse {

  @Schema(description = "수신 여부", example = "true")
  private Boolean received;

  @Schema(description = "Content UUID")
  private UUID contentId;
}
