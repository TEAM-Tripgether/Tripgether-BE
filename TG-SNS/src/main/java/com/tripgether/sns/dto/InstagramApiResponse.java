package com.tripgether.sns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Instagram API 응답 캡처 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstagramApiResponse {

  /**
   * 캡처된 API 응답 (JSON 문자열)
   */
  private String apiResponse;

  /**
   * 처리 소요 시간 (밀리초)
   */
  private Long elapsedTimeMs;

  /**
   * Instagram 게시물 URL
   */
  private String instagramUrl;
}
