package com.tripgether.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tripgether.common.constant.SocialPlatform;
import com.tripgether.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AuthRequest {

    @Schema(hidden = true, description = "회원")
    @JsonIgnore
    private Member member;

    /*@Schema(description = "로그인 플랫폼 (KAKAO, GOOGLE 등)", example = "KAKAO")
    private SocialPlatform socialPlatform;*/

    @Schema(description = "소셜 로그인 후 반환된 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "소셜 로그인 후 반환된 닉네임", example = "홍길동")
    private String name;

    /*@Schema(description = "소셜 로그인 후 반환된 프로필 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;*/

    @Schema(hidden = true)
    private String accessToken;

    @Schema(hidden = true)
    private String refreshToken;
}
