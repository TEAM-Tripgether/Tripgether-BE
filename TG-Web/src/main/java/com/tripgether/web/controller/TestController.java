package com.tripgether.web.controller;

import com.tripgether.application.dto.TestRequest;
import com.tripgether.application.dto.TestResponse;
import com.tripgether.application.service.TestService;
import com.tripgether.member.dto.FcmNotificationRequest;
import com.tripgether.member.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 컨트롤러
 * 항상 같은 구조의 TestResponse를 반환
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/test")
@Tag(name = "테스트 API", description = "테스트용 API 제공")
public class TestController {

  private final TestService testService;
  private final FcmService fcmService;

  @PostMapping("/mock-content")
  @Operation(summary = "Mock Content 생성 및 반환")
  public ResponseEntity<TestResponse> createMockContent(
      @RequestBody TestRequest request
  ) {
    return ResponseEntity.ok(testService.createMockContent(request));
  }

  /**
   * FCM 푸시 알림 전송 테스트
   */
  @PostMapping("/fcm/send")
  @Operation(summary = "FCM 푸시 알림 전송 테스트", description = "단일 기기로 FCM 푸시 알림을 전송합니다. fcmToken 필드에 실제 FCM 토큰을 입력해야 합니다.")
  public ResponseEntity<String> sendTestNotification(
      @Valid @RequestBody FcmNotificationRequest request) {

      fcmService.sendNotification(
          request.getFcmToken(),
          request.getTitle(),
          request.getBody(),
          request.getData(),
          request.getImageUrl());

      return ResponseEntity.accepted().build();
  }
}

