package com.tripgether.web.controller;

import com.tripgether.ai.dto.AiCallbackRequest;
import com.tripgether.ai.dto.AiCallbackResponse;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.properties.AiServerProperties;
import com.tripgether.common.util.CommonUtil;
import com.tripgether.sns.service.AiCallbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
  private final CommonUtil commonUtil;

  @PostMapping("/callback")
  @Override
  public ResponseEntity<AiCallbackResponse> handleCallback(
      @RequestHeader(value = "X-API-Key", required = true) String apiKey,
      @Valid @RequestBody AiCallbackRequest request) {

    // ContentInfo에서 contentId 추출
    UUID contentId = request.getContentInfo() != null && request.getContentInfo().getContentId() != null
        ? request.getContentInfo().getContentId()
        : null;

    log.debug("AI callback received: contentId={}, resultStatus={}",
        contentId, request.getResultStatus());

    // API Key 검증
    if (!aiServerProperties.getCallbackApiKey().equals(apiKey)) {
      log.error("Invalid API Key from AI server. Expected: {}, Received: {}",
              commonUtil.maskSecureString(aiServerProperties.getCallbackApiKey()),
              commonUtil.maskSecureString(apiKey));
      throw new CustomException(ErrorCode.INVALID_API_KEY);
    }

    // Callback 처리
    aiCallbackService.processAiServerCallback(request);

    // 응답 생성
    AiCallbackResponse response = AiCallbackResponse.builder()
        .received(true)
        .contentId(contentId)
        .build();

    log.info("AI callback processed successfully: contentId={}", contentId);

    return ResponseEntity.ok(response);
  }
}
