package com.tripgether.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripgether.auth.dto.SecurityUrls;
import com.tripgether.auth.jwt.JwtUtil;
import com.tripgether.auth.service.CustomUserDetailsService;
import com.tripgether.common.exception.ErrorResponse;
import com.tripgether.common.exception.constant.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰 기반 인증 필터
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("요청된 URI: {}", uri);

        // 화이트리스트 체크 : 화이트리스트 경로면 필터링 건너뜀
        if (isWhitelistedPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 타입 구분 : API 요청만 처리
        boolean isApiRequest = uri.startsWith("/api/");

        try {
            String token = null;
            String bearerToken = request.getHeader("Authorization");
            
            // API 요청 : Authorization 헤더에서 "Bearer " 토큰 추출
            if (isApiRequest) {
                log.debug("일반 API 요청입니다.");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7).trim(); // "Bearer " 제거
                }
            }

            // 토큰 검증: 토큰이 유효하면 인증 설정
            if (token != null && jwtUtil.validateToken(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 인증 성공
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰이 없거나 유효하지 않은 경우
            if (isApiRequest) {
                // 토큰 없음
                if (token == null) {
                    log.error("토큰이 존재하지 않습니다.");
                    sendErrorResponse(response, ErrorCode.MISSING_AUTH_TOKEN);
                } else { // 유효하지 않은 토큰
                    log.error("토큰이 유효하지 않습니다.");
                    sendErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
                }
                return; // 필터 체인 진행하지 않음
            }
        } catch (ExpiredJwtException e) {
            log.error("토큰 만료: {}", e.getMessage());
            // 토큰 만료 예외 처리
            if (isApiRequest) {
                sendErrorResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
            }
            return;
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 에러 응답을 JSON 형태로 클라이언트에 전송
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.getResponse(errorCode);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * 화이트리스트 경로 확인 (인증x)
     */
    private boolean isWhitelistedPath(String uri) {
        return SecurityUrls.AUTH_WHITELIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}

