package com.tripgether.web.controller;

import com.tripgether.auth.dto.AuthRequest;
import com.tripgether.auth.dto.AuthResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.common.constant.Author;
import io.swagger.v3.oas.annotations.Operation;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.10.16",
          author = Author.SUHSAECHAN,
          issueNumber = 22,
          description = "인증 모듈 추가 및 기본 OAuth 로그인 구현")
  })
  @Operation(
      summary = "소셜 로그인",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터 (AuthRequest)
              - **`socialPlatform`**: 로그인 플랫폼 (KAKAO, GOOGLE)
              - **`email`**: 사용자 이메일
              - **`name`**: 사용자 닉네임
              - **`profileUrl`**: 사용자 프로필 url (선택)
              
              ## 반환값 (AuthResponse)
              - **`accessToken`**: 발급된 AccessToken
              - **`refreshToken`**: 발급된 RefreshToken
              - **`isFirstLogin`**: 최초 로그인 여부
              
              ## 특이사항
              - 클라이언트에서 Kakao/Google OAuth 처리 후 받은 사용자 정보로 서버에 JWT 토큰을 요청합니다.
              - 액세스 토큰은 1시간, 리프레시 토큰은 7일 유효합니다.
              
              ## 에러코드
              - **`INVALID_SOCIAL_TOKEN`**: 유효하지 않은 소셜 인증 토큰입니다.
              - **`SOCIAL_AUTH_FAILED`**: 소셜 로그인 인증에 실패하였습니다.
              - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
              """)
  ResponseEntity<AuthResponse> signIn(AuthRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "토큰 재발급 기능"
          + " 구현")
  })
  @Operation(
      summary = "토큰 재발급",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터 (AuthRequest)
              - **`refreshToken`**: 리프레시 토큰
              
              ## 반환값 (AuthResponse)
              - **`accessToken`**: 재발급된 AccessToken
              - **`refreshToken`**: 리프레시 토큰 (변경되지 않음)
              - **`isFirstLogin`**: 최초 로그인 여부
              
              ## 특이사항
              - 만료된 액세스 토큰을 리프레시 토큰으로 재발급합니다.
              
              ## 에러코드
              - **`REFRESH_TOKEN_NOT_FOUND`**: 리프레시 토큰을 찾을 수 없습니다.
              - **`INVALID_REFRESH_TOKEN`**: 유효하지 않은 리프레시 토큰입니다.
              - **`EXPIRED_REFRESH_TOKEN`**: 만료된 리프레시 토큰입니다.
              - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
              """)
  ResponseEntity<AuthResponse> reissue(AuthRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "로그아웃 기능 구현")
  })
  @Operation(
      summary = "로그아웃",
      description =
          """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터 (AuthRequest)
              - **`accessToken`**: 엑세스 토큰 (Header에서 자동 추출)
              - **`refreshToken`**: 리프레시 토큰
              
              ## 반환값
              - 성공 시 상태코드 200 (OK)와 빈 응답 본문
              
              ## 동작 설명
              - 액세스 토큰을 블랙리스트에 등록하여 무효화 처리
              - Redis에 저장된 리프레시 토큰 삭제
              
              ## 에러코드
              - **`INVALID_TOKEN`**: 유효하지 않은 토큰입니다.
              - **`UNAUTHORIZED`**: 인증이 필요한 요청입니다.
              """)
  ResponseEntity<Void> logout(CustomUserDetails customUserDetails, String authorization, AuthRequest request);
}
