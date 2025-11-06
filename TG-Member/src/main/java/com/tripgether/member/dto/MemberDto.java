package com.tripgether.member.dto;

import com.tripgether.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 DTO")
public class MemberDto {

  @Schema(description = "회원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Schema(description = "이메일", example = "user@example.com", required = true)
  private String email;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
  @Schema(description = "닉네임", example = "여행러버", required = true)
  private String name;

  @Schema(description = "회원 상태", example = "NOT_STARTED")
  private String onboardingStatus;

  //필수 약관 동의 여부
  @Schema(description = "필수 약관 동의 여부", example = "true")
  private boolean requiredAgreed;

  //마케팅 수신 동의 여부(선택)
  @Schema(description = "마케팅 수신 동의 여부(선택)", example = "false")
  private boolean marketingAgreed;

  //약관 버전(예: v1.0)
  @Size(max = 20, message = "약관 버전은 20자를 초과할 수 없습니다.")
  @Schema(description = "약관 버전", example = "v1.0")
  private String termsVersion;

  public static MemberDto entityToDto(Member entity) {
    return MemberDto.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .name(entity.getName())
        .onboardingStatus(entity.getOnboardingStatus().name())
        .requiredAgreed(Boolean.TRUE.equals(entity.isRequiredAgreed()))
        .marketingAgreed(Boolean.TRUE.equals(entity.isMarketingAgreed()))
        .termsVersion(entity.getTermsVersion())
        .build();
  }
}
