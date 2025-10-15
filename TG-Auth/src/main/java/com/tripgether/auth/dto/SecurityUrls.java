package com.tripgether.auth.dto;

import java.util.List;

/**
 * Security 관련 URL 상수 관리
 */
public class SecurityUrls {

    /**
     * 인증을 생략할 URL 패턴 목록
     */
    public static final List<String> AUTH_WHITELIST = List.of(
            // Auth
            "/api/auth/sign-in",    // 소셜 로그인
            "/api/auth/reissue",    // 토큰 재발급

            // Swagger
            "/swagger-ui/**",       // Swagger UI
            "/v3/api-docs/**",      // Swagger API 문서
            "/swagger-ui.html",     // Swagger UI HTML

            // Actuator (선택사항)
            "/actuator/**",

            // 기타 정적 리소스
            "/favicon.ico",
            "/error"
    );
}

