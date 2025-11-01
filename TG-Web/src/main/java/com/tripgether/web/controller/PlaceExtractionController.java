package com.tripgether.web.controller;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/front")
//@Tag()
public class PlaceExtractionController {
  private final AiServerService aiServerService;

  /**
   * 프론트에서 전달한 SNS URL을 받아 AI 서버에 장소 추출을 의뢰합니다.
   * - 컨트롤러는 URL 수신/검증/로그만 담당
   * - 비즈니스 처리는 AiServerService로 위임
   */
  @PostMapping("/place-extraction")
  @Operation(summary = "장소 추출 요청 접수", description = "SNS URL을 받아 AI 서버로 장소 추출을 요청합니다.")
  public ResponseEntity<PlaceExtractionResponse> requestPlaceExtraction(
      @Valid @RequestBody PlaceExtractionRequest request
  ) {
    log.info("[PlaceExtraction] 요청 수신 - contentId={}, url={}",
        request.getContentId(), request.getSnsUrl());

    // (필요 시) 추가적인 얕은 검증
    if (request.getSnsUrl() == null || request.getSnsUrl().isBlank()) {
      log.warn("[PlaceExtraction] 유효하지 않은 URL 입력 - contentId={}", request.getContentId());
      // 여기서 예외를 던지거나, 400 반환(공통 예외 체계 사용 시 CustomException 권장)
      return ResponseEntity.badRequest().build();
    }

    // 실제 처리
    PlaceExtractionResponse response = aiServerService.requestPlaceExtraction(request.getContentId(), request.getSnsUrl());

    log.info("[PlaceExtraction] 요청 위임 완료 - contentId={}",
        request.getContentId());

    return ResponseEntity.ok(response);
  }
}
