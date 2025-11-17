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

  @Schema(description = "추출된 장소 목록")
  @Valid
  private List<PlaceInfo> places;
}
