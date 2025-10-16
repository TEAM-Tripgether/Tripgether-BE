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
    private UUID memberId;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
    @Schema(description = "닉네임", example = "여행러버", required = true)
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "회원 상태", example = "ACTIVE")
    private String status;

    public static MemberDto entityToDto(Member entity) {
        return MemberDto.builder()
                .memberId(entity.getMemberId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .profileImageUrl(entity.getProfileImageUrl())
                .status(
                        entity.getStatus()
                                .name())
                .build();
    }
}
