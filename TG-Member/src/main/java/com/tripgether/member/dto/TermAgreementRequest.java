package com.tripgether.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermAgreementRequest {
  @Schema(description = "이용약관 동의 여부", example = "true")
  @NotNull Boolean agreedToTerms;

  @Schema(description = "개인정보처리방침 약관 동의 여부", example = "true")
  @NotNull Boolean agreedToPrivacy;

  @Schema(description = "마케팅(선택) 약관 동의 여부", example = "false")
  private Boolean agreedToMarketing;

  @Schema(description = "약관 버전", example = "v1.0")
  private String termsVersion;
}

//TODO 합치고 변수명 통일하기
