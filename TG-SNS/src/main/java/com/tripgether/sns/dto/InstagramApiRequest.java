package com.tripgether.sns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Instagram API 응답 캡처 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstagramApiRequest {

  @NotBlank(message = "Instagram URL은 필수입니다")
  @Pattern(regexp = "^https?://www\\.instagram\\.com/.*", message = "유효한 Instagram URL이 아닙니다")
  private String instagramUrl;
}
