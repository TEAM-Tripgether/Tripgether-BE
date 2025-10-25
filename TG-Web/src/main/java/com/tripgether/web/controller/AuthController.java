package com.tripgether.web.controller;

import com.tripgether.auth.dto.AuthRequest;
import com.tripgether.auth.dto.AuthResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.auth.service.AuthService;
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
public class AuthController implements AuthControllerDocs {

  private final AuthService authService;

  @PostMapping("/sign-in")
  public ResponseEntity<AuthResponse> signIn(@RequestBody AuthRequest request) {
    log.debug("소셜 로그인 요청: {}", request);
    return ResponseEntity.ok(authService.signIn(request));
  }

  @PostMapping("/reissue")
  public ResponseEntity<AuthResponse> reissue(@RequestBody AuthRequest request) {
    log.debug("토큰 재발급 요청");
    return ResponseEntity.ok(authService.reissue(request));
  }

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
