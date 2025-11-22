package com.tripgether.auth.service;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tripgether.auth.dto.SignInRequest;
import com.tripgether.auth.dto.SignInResponse;
import com.tripgether.common.constant.DeviceType;
import com.tripgether.common.exception.CustomException;
import com.tripgether.member.entity.FcmToken;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.FcmTokenRepository;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.web.TripgetherApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest(classes = TripgetherApplication.class)
@ActiveProfiles("dev")
@Transactional
@Slf4j
class AuthServiceFcmTokenTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private FcmTokenRepository fcmTokenRepository;

  private static final String TEST_EMAIL = "fcmtest@example.com";
  private static final String TEST_FCM_TOKEN = "test-fcm-token-123";
  private static final String TEST_DEVICE_ID = "550e8400-e29b-41d4-a716-446655440000";

  @BeforeEach
  void setUp() {
    // 테스트 전 데이터 정리
    memberRepository.findByEmail(TEST_EMAIL).ifPresent(member -> {
      fcmTokenRepository.deleteByMember(member);
      memberRepository.delete(member);
    });
  }

  @Test
  @DisplayName("FCM 토큰과 함께 로그인하면 FCM 토큰이 저장된다")
  void signIn_WithFcmToken_SavesFcmToken() {
    lineLog("=== FCM 토큰 저장 테스트 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken(TEST_FCM_TOKEN)
        .deviceType(DeviceType.IOS)
        .deviceId(TEST_DEVICE_ID)
        .build();

    // when
    SignInResponse response = authService.signIn(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getAccessToken()).isNotBlank();

    Member member = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();
    List<FcmToken> tokens = fcmTokenRepository.findByMember(member);

    assertThat(tokens).hasSize(1);
    FcmToken savedToken = tokens.get(0);
    assertThat(savedToken.getFcmToken()).isEqualTo(TEST_FCM_TOKEN);
    assertThat(savedToken.getDeviceType()).isEqualTo(DeviceType.IOS);
    assertThat(savedToken.getDeviceId()).isEqualTo(TEST_DEVICE_ID);
    assertThat(savedToken.getLastUsedAt()).isNotNull();

    log.info("FCM 토큰 저장 성공: {}", savedToken.getFcmToken());
    lineLog("=== FCM 토큰 저장 테스트 종료 ===");
  }

  @Test
  @DisplayName("FCM 토큰 없이 로그인하면 FCM 토큰이 저장되지 않는다")
  void signIn_WithoutFcmToken_DoesNotSaveFcmToken() {
    lineLog("=== FCM 토큰 없이 로그인 테스트 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .build();

    // when
    SignInResponse response = authService.signIn(request);

    // then
    assertThat(response).isNotNull();

    Member member = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();
    List<FcmToken> tokens = fcmTokenRepository.findByMember(member);

    assertThat(tokens).isEmpty();

    lineLog("FCM 토큰 없이 로그인 성공");
    lineLog("=== FCM 토큰 없이 로그인 테스트 종료 ===");
  }

  @Test
  @DisplayName("동일 디바이스로 재로그인하면 FCM 토큰이 업데이트된다")
  void signIn_SameDevice_UpdatesFcmToken() {
    lineLog("=== FCM 토큰 업데이트 테스트 시작 ===");

    // given - 첫 번째 로그인
    SignInRequest firstRequest = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken("old-fcm-token")
        .deviceType(DeviceType.IOS)
        .deviceId(TEST_DEVICE_ID)
        .build();
    authService.signIn(firstRequest);

    // when - 동일 디바이스로 새 토큰과 함께 재로그인
    SignInRequest secondRequest = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken("new-fcm-token")
        .deviceType(DeviceType.ANDROID)
        .deviceId(TEST_DEVICE_ID)
        .build();
    authService.signIn(secondRequest);

    // then
    Member member = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();
    List<FcmToken> tokens = fcmTokenRepository.findByMember(member);

    assertThat(tokens).hasSize(1);
    FcmToken updatedToken = tokens.get(0);
    assertThat(updatedToken.getFcmToken()).isEqualTo("new-fcm-token");
    assertThat(updatedToken.getDeviceType()).isEqualTo(DeviceType.ANDROID);

    log.info("FCM 토큰 업데이트 성공: old-fcm-token -> {}", updatedToken.getFcmToken());
    lineLog("=== FCM 토큰 업데이트 테스트 종료 ===");
  }

  @Test
  @DisplayName("다른 디바이스로 로그인하면 멀티 디바이스 FCM 토큰이 저장된다")
  void signIn_DifferentDevices_SavesMultipleFcmTokens() {
    lineLog("=== 멀티 디바이스 FCM 토큰 저장 테스트 시작 ===");

    // given & when - 아이폰으로 로그인
    SignInRequest iphoneRequest = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken("iphone-fcm-token")
        .deviceType(DeviceType.IOS)
        .deviceId("iphone-device-id")
        .build();
    authService.signIn(iphoneRequest);

    // when - 아이패드로 로그인
    SignInRequest ipadRequest = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken("ipad-fcm-token")
        .deviceType(DeviceType.IOS)
        .deviceId("ipad-device-id")
        .build();
    authService.signIn(ipadRequest);

    // then
    Member member = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();
    List<FcmToken> tokens = fcmTokenRepository.findByMember(member);

    assertThat(tokens).hasSize(2);
    assertThat(tokens)
        .extracting(FcmToken::getDeviceId)
        .containsExactlyInAnyOrder("iphone-device-id", "ipad-device-id");

    log.info("멀티 디바이스 FCM 토큰 저장 성공: {} 개", tokens.size());
    lineLog("=== 멀티 디바이스 FCM 토큰 저장 테스트 종료 ===");
  }

  @Test
  @DisplayName("fcmToken만 있고 deviceType이 없으면 예외가 발생한다")
  void signIn_FcmTokenWithoutDeviceType_ThrowsException() {
    lineLog("=== FCM 토큰 검증 테스트 (deviceType 누락) 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken(TEST_FCM_TOKEN)
        .deviceId(TEST_DEVICE_ID)
        // deviceType 누락
        .build();

    // when & then
    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(CustomException.class);

    lineLog("deviceType 누락 시 예외 발생 확인");
    lineLog("=== FCM 토큰 검증 테스트 종료 ===");
  }

  @Test
  @DisplayName("fcmToken만 있고 deviceId가 없으면 예외가 발생한다")
  void signIn_FcmTokenWithoutDeviceId_ThrowsException() {
    lineLog("=== FCM 토큰 검증 테스트 (deviceId 누락) 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken(TEST_FCM_TOKEN)
        .deviceType(DeviceType.IOS)
        // deviceId 누락
        .build();

    // when & then
    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(CustomException.class);

    lineLog("deviceId 누락 시 예외 발생 확인");
    lineLog("=== FCM 토큰 검증 테스트 종료 ===");
  }

  @Test
  @DisplayName("deviceType과 deviceId만 있고 fcmToken이 없으면 예외가 발생한다")
  void signIn_DeviceInfoWithoutFcmToken_ThrowsException() {
    lineLog("=== FCM 토큰 검증 테스트 (fcmToken 누락) 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .deviceType(DeviceType.IOS)
        .deviceId(TEST_DEVICE_ID)
        // fcmToken 누락
        .build();

    // when & then
    assertThatThrownBy(() -> authService.signIn(request))
        .isInstanceOf(CustomException.class);

    lineLog("fcmToken 누락 시 예외 발생 확인");
    lineLog("=== FCM 토큰 검증 테스트 종료 ===");
  }

  @Test
  @DisplayName("Member ID로 FCM 토큰을 조회할 수 있다")
  void findFcmTokensByMemberId() {
    lineLog("=== Member ID로 FCM 토큰 조회 테스트 시작 ===");

    // given
    SignInRequest request = SignInRequest.builder()
        .email(TEST_EMAIL)
        .name("테스트유저")
        .fcmToken(TEST_FCM_TOKEN)
        .deviceType(DeviceType.IOS)
        .deviceId(TEST_DEVICE_ID)
        .build();
    authService.signIn(request);

    Member member = memberRepository.findByEmail(TEST_EMAIL).orElseThrow();

    // when
    List<FcmToken> tokens = fcmTokenRepository.findByMemberId(member.getId());

    // then
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0).getFcmToken()).isEqualTo(TEST_FCM_TOKEN);

    lineLog("Member ID로 FCM 토큰 조회 성공");
    lineLog("=== Member ID로 FCM 토큰 조회 테스트 종료 ===");
  }
}
