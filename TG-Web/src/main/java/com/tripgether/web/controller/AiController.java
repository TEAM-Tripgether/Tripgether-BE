package com.tripgether.web.controller;

import com.tripgether.ai.dto.AiCallbackRequest;
import com.tripgether.ai.dto.AiCallbackResponse;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.properties.AiServerProperties;
import com.tripgether.sns.service.AiCallbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 서버 Webhook Callback을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ai")
@Tag(name = "AI 서버 API", description = "AI 서버 연동 관련 API 제공")
public class AiController implements AiControllerDocs {

  private final AiCallbackService aiCallbackService;
  private final AiServerProperties aiServerProperties;

  @PostMapping("/callback")
  @Override
  public ResponseEntity<AiCallbackResponse> handleCallback(
      @RequestHeader(value = "X-API-Key", required = true) String apiKey,
      @Valid @RequestBody AiCallbackRequest request) {

    log.debug("AI callback received: contentId={}, resultStatus={}",
        request.getContentId(), request.getResultStatus());

    // API Key 검증
    if (!aiServerProperties.getCallbackApiKey().equals(apiKey)) {
      log.error("Invalid API Key from AI server. Expected: {}, Received: {}",
          maskApiKey(aiServerProperties.getCallbackApiKey()),
          maskApiKey(apiKey));
      throw new CustomException(ErrorCode.INVALID_API_KEY);
    }

    // Callback 처리
    aiCallbackService.processAiServerCallback(request);

    // 응답 생성
    AiCallbackResponse response = AiCallbackResponse.builder()
        .received(true)
        .contentId(request.getContentId())
        .build();

    log.info("AI callback processed successfully: contentId={}", request.getContentId());

    return ResponseEntity.ok(response);
  }

  /**
   * API Key를 마스킹하여 로그에 출력합니다 (보안)
   *
   * @param apiKey API Key
   * @return 마스킹된 API Key (앞 4자리만 표시)
   */
  private String maskApiKey(String apiKey) {
    if (apiKey == null || apiKey.length() < 8) {
      return "****";
    }
    return apiKey.substring(0, 4) + "****";
  }
}
