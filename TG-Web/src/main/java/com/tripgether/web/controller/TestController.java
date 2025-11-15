package com.tripgether.web.controller;

import com.tripgether.application.dto.TestRequest;
import com.tripgether.application.dto.TestResponse;
import com.tripgether.application.service.TestService;
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

  @PostMapping("/mock-content")
  @Operation(summary = "Mock Content 생성 및 반환")
  public ResponseEntity<TestResponse> createMockContent(
      @RequestBody TestRequest request
  ) {
    return ResponseEntity.ok(testService.createMockContent(request));
  }
}

