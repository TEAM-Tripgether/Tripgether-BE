package com.tripgether.auth.service;

import com.tripgether.auth.dto.AuthRequest;
import com.tripgether.auth.dto.AuthResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.auth.jwt.JwtUtil;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.member.constant.MemberOnboardingStatus;
import com.tripgether.member.constant.OnboardingStep;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.member.service.MemberService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private static final String REFRESH_KEY_PREFIX = "RT:";

  private final MemberRepository memberRepository;
  private final MemberService memberService;
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * 로그인 로직 클라이언트로부터 플랫폼, 닉네임, 프로필url, 이메일을 입력받아 JWT를 발급합니다.
   */
  @Transactional
  public AuthResponse signIn(AuthRequest request) {
    // 요청 값으로부터 사용자 정보 획득
    String email = request.getEmail();
    String name = request.getName();

    //회원 조회
    Member member = memberRepository.findByEmail(email)
        .orElseGet(() -> {
          // 신규 회원 생성 시 기본값 자동 설정
          Member newMember = Member.builder()
              .email(email)
              .name(name)
              .build();
          memberRepository.save(newMember);
          log.debug("신규 회원 가입: {}", email);
          return newMember;
        });

    boolean isFirstLogin = member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED;

    //온보딩 상태 갱신 (NOT_STARTED → IN_PROGRESS)
    if (isFirstLogin) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
      memberRepository.save(member);
      log.debug("온보딩 상태 변경: {} → {}", MemberOnboardingStatus.NOT_STARTED, MemberOnboardingStatus.IN_PROGRESS);
    } else {
      log.debug("기존 회원 로그인: {}", email);
    }

    // JWT 토큰 생성
    CustomUserDetails customUserDetails = new CustomUserDetails(member);
    String accessToken = jwtUtil.createAccessToken(customUserDetails);
    String refreshToken = jwtUtil.createRefreshToken(customUserDetails);

    log.debug("로그인 성공: email={}, accessToken={}, refreshToken={}", email, accessToken, refreshToken);

    // RefreshToken -> Redis 저장 (키: "RT:{memberId}")
    redisTemplate.opsForValue().set(
        REFRESH_KEY_PREFIX + customUserDetails.getMemberId(),
        refreshToken,
        jwtUtil.getRefreshExpirationTime(),
        TimeUnit.MILLISECONDS);

    //온보딩 필요 여부 확인
    boolean requiresOnboarding = (member.getOnboardingStatus() != MemberOnboardingStatus.COMPLETED);

    // 온보딩 단계 계산 및 저장
    // COMPLETED 상태면 계산하지 않고 캐시된 값 사용
    String onboardingStep;
    if (member.getOnboardingStatus() == MemberOnboardingStatus.COMPLETED) {
      // COMPLETED 상태면 캐시된 값 사용 (없으면 COMPLETED 반환)
      onboardingStep = member.getOnboardingStep() != null 
          ? member.getOnboardingStep().name() 
          : OnboardingStep.COMPLETED.name();
    } else {
      // IN_PROGRESS 또는 NOT_STARTED 상태면 계산 후 저장
      OnboardingStep step = memberService.calculateAndSaveOnboardingStep(member);
      onboardingStep = step.name();
    }

    //응답 생성
    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isFirstLogin(isFirstLogin)
        .requiresOnboarding(requiresOnboarding)
        .onboardingStep(onboardingStep)
        .build();
  }

  /**
   * refreshToken을 통해 accessToken을 재발급합니다
   */
  @Transactional
  public AuthResponse reissue(AuthRequest request) {
    log.debug("accessToken이 만료되어 토큰 재발급을 진행합니다.");

    String refreshToken = request.getRefreshToken();

    // 리프레시 토큰이 없는 경우
    if (refreshToken == null || refreshToken.isBlank()) {
      log.error("refreshToken을 찾을 수 없습니다.");
      throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    // 리프레시 토큰 유효성 검사 및 만료 여부 확인
    try {
      if (!jwtUtil.validateToken(refreshToken)) {
        log.error("유효하지 않은 refreshToken 입니다.");
        throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
      }
    } catch (ExpiredJwtException e) {
      log.error("만료된 refreshToken 입니다: {}", e.getMessage());
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    // 새로운 accessToken 생성
    CustomUserDetails customUserDetails = (CustomUserDetails) jwtUtil.getAuthentication(refreshToken).getPrincipal();

    // Redis에 저장된 refreshToken과 일치 여부 확인
    String refreshKey = REFRESH_KEY_PREFIX + customUserDetails.getMemberId();
    String storedRefreshToken = (String) redisTemplate.opsForValue().get(refreshKey);

    if (storedRefreshToken == null) {
      log.error("Redis에 저장된 refreshToken을 찾을 수 없습니다. memberId: {}", customUserDetails.getMemberId());
      throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_STORED);
    }

    if (!storedRefreshToken.equals(refreshToken)) {
      log.error("Redis에 저장된 refreshToken과 일치하지 않습니다. memberId: {}", customUserDetails.getMemberId());
      throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    String newAccessToken = jwtUtil.createAccessToken(customUserDetails);

    // 회원 존재 여부 검증
    memberRepository.findByEmail(jwtUtil.getUsername(newAccessToken))
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .isFirstLogin(false)
        .build();
  }

  /**
   * 로그아웃 액세스 토큰을 블랙리스트에 등록합니다 redis에 저장되어있는 리프레시토큰을 삭제합니다
   */
  @Transactional
  public void logout(AuthRequest request) {
    Member member = request.getMember();
    String accessToken = request.getAccessToken();

    // accessToken 입력값 검증
    if (member == null || accessToken == null || accessToken.isBlank()) {
      log.error("로그아웃 요청에 필수 정보가 누락되었습니다.");
      throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }

    // 저장된 refreshToken 키
    String key = REFRESH_KEY_PREFIX + member.getId();

    // 토큰 비활성화
    jwtUtil.deactivateToken(accessToken, key);
  }
}
