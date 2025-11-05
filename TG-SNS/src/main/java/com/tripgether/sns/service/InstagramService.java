package com.tripgether.sns.service;

import com.google.gson.JsonArray;
import com.tripgether.common.util.InstagramApiCapture;
import com.tripgether.sns.dto.InstagramApiRequest;
import com.tripgether.sns.dto.InstagramApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Instagram API 응답 캡처 서비스
 * Lazy 초기화를 통해 실제 사용 시점에만 의존성을 생성합니다.
 */
@Service
@Lazy
@RequiredArgsConstructor
@Slf4j
public class InstagramService {

  private final InstagramApiCapture instagramApiCapture;

  /**
   * Instagram URL로부터 API 응답을 캡처합니다.
   *
   * @param request Instagram API 요청
   * @return Instagram API 응답
   */
  public InstagramApiResponse captureInstagramApiResponse(InstagramApiRequest request) {
    long startTime = System.currentTimeMillis();

    log.info("Instagram API 응답 캡처 요청: {}", request.getInstagramUrl());

    // API 응답 캡처
    JsonArray apiResponseArray = instagramApiCapture.captureApiResponse(request.getInstagramUrl());

    // JsonArray를 String으로 변환 (Pretty JSON)
    String apiResponse = apiResponseArray.toString();

    long elapsedTime = System.currentTimeMillis() - startTime;

    log.info("Instagram API 응답 캡처 완료: 소요시간={}ms", elapsedTime);

    return InstagramApiResponse.builder()
        .apiResponse(apiResponse)
        .elapsedTimeMs(elapsedTime)
        .instagramUrl(request.getInstagramUrl())
        .build();
  }
}
