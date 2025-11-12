package com.tripgether.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테스트용 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {

  @Schema(description = "콘텐츠 개수", example = "1", defaultValue = "1")
  private int contentCount;
}

