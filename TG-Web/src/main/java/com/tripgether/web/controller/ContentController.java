package com.tripgether.web.controller;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.PlaceResponse;
import com.tripgether.sns.dto.RecentContentResponse;
import com.tripgether.sns.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<RequestPlaceExtractionResponse> createContentAndRequestPlaceExtraction(
      //@AuthenticationPrincipal CustomUserDetails userDetails, // JWT 인증
      //내부에서 SecurityContextHolder.getContext().getAuthentication()로 꺼내 사용
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody PlaceExtractionRequest request
  ) {
    UUID memberId = userDetails.getMemberId();

    RequestPlaceExtractionResponse response
        = contentService.createContentAndRequestPlaceExtraction(request, memberId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/content/recent")
  public ResponseEntity<List<RecentContentResponse>> getRecentContents(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<RecentContentResponse> contents =
        contentService.getRecentContents(userDetails.getMemberId());
    return ResponseEntity.ok(contents);
  }

  /**
   * 사용자가 저장한 장소 목록 조회 (최신순, 최대 10개)
   */
  @GetMapping("/place/saved")
  @Operation(summary = "저장한 장소 목록 조회",
      description = "사용자가 저장한 장소 정보를 최신순으로 최대 10개까지 조회합니다.")
  public ResponseEntity<List<PlaceResponse>> getSavedPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<PlaceResponse> responses =
        contentService.getSavedPlaces(userDetails.getMemberId());
    return ResponseEntity.ok(responses);
  }
}
