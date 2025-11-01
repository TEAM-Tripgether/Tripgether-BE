package com.tripgether.web.controller;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.sns.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/content")
//@Tag()
public class ContentController implements ContentControllerDocs {
  private final ContentService contentService;

  /**
   * 프론트에서 전달한 SNS URL을 받아 AI 서버에 장소 추출을 의뢰합니다.
   * - 컨트롤러는 URL 수신/검증/로그만 담당
   * - 비즈니스 처리는 AiServerService로 위임
   */
  @PostMapping("/analyze")
  @Override
  public ResponseEntity<RequestPlaceExtractionResponse> createContent(
      //@AuthenticationPrincipal CustomUserDetails userDetails, // JWT 인증
      //내부에서 SecurityContextHolder.getContext().getAuthentication()로 꺼내 사용
      @Valid @RequestBody PlaceExtractionRequest request
  ) {
    RequestPlaceExtractionResponse response
        = contentService.createContentAndRequestPlaceExtraction(request);
    return ResponseEntity.ok(response);
  }
}
