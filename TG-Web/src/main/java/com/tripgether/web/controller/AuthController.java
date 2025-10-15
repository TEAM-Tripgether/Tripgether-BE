package com.tripgether.web.controller;

import com.tripgether.auth.dto.AuthRequest;
import com.tripgether.auth.dto.AuthResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 API", description = "회원 인증(소셜 로그인) 관련 API 제공")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "소셜 로그인",
            description = """
                    ## 소셜 로그인 API
                    
                    클라이언트에서 Kakao/Google OAuth 처리 후, 받은 사용자 정보로 서버에 JWT 토큰을 요청합니다.
                    
                    **요청 파라미터:**
                    - socialPlatform: 소셜 플랫폼 (KAKAO, GOOGLE)
                    - email: 이메일
                    - nickname: 닉네임
                    - profileUrl: 프로필 이미지 URL (선택)
                    
                    **응답:**
                    - accessToken: 액세스 토큰 (1시간)
                    - refreshToken: 리프레시 토큰 (7일)
                    - isFirstLogin: 첫 로그인 여부
                    """
    )
    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request) {
        log.debug("소셜 로그인 요청: {}", request);
        return ResponseEntity.ok(authService.signIn(request));
    }

    @Operation(
            summary = "토큰 재발급",
            description = """
                    ## 액세스 토큰 재발급 API
                    
                    만료된 액세스 토큰을 리프레시 토큰으로 재발급합니다.
                    
                    **요청 파라미터:**
                    - refreshToken: 리프레시 토큰
                    
                    **응답:**
                    - accessToken: 새로운 액세스 토큰
                    """
    )
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(@RequestBody AuthRequest request) {
        log.debug("토큰 재발급 요청");
        return ResponseEntity.ok(authService.reissue(request));
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    ## 로그아웃 API
                    
                    액세스 토큰을 블랙리스트에 등록하고, 리프레시 토큰을 삭제합니다.
                    
                    **인증 필요:** Bearer Token
                    """
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody AuthRequest request) {
        log.debug("로그아웃 요청: {}", customUserDetails.getUsername());
        request.setMember(customUserDetails.getMember());

        if (authorization != null && authorization.startsWith("Bearer ")) {
            request.setAccessToken(authorization.substring(7).trim());
        }

        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}

