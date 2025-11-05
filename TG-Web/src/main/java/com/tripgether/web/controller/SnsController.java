package com.tripgether.web.controller;

import com.tripgether.sns.dto.InstagramApiRequest;
import com.tripgether.sns.dto.InstagramApiResponse;
import com.tripgether.sns.service.InstagramService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SNS 관련 API를 처리하는 컨트롤러
 */
@RestController
@Slf4j
@RequestMapping("/api/sns")
@Tag(name = "SNS API", description = "SNS 관련 API 제공")
public class SnsController implements SnsControllerDocs {

  private final InstagramService instagramService;

  /**
   * Constructor with @Lazy injection to prevent eager initialization
   */
  public SnsController(@Lazy InstagramService instagramService) {
    this.instagramService = instagramService;
  }

  @PostMapping("/instagram/capture")
  @Override
  public ResponseEntity<InstagramApiResponse> captureInstagramApi(
      @Valid @RequestBody InstagramApiRequest request) {

    log.info("Instagram API 응답 캡처 요청: {}", request.getInstagramUrl());

    // Instagram API 응답 캡처
    InstagramApiResponse response = instagramService.captureInstagramApiResponse(request);

    log.info("Instagram API 응답 캡처 완료: contentId={}, elapsedTime={}ms",
        response.getInstagramUrl(), response.getElapsedTimeMs());

    return ResponseEntity.ok(response);
  }
}
