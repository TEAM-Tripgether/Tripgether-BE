package com.tripgether.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Snapinsta.to에서 Cloudflare Turnstile 토큰(cftoken)을 자동으로 추출하는 유틸리티
 *
 * Playwright를 사용하여 실제 브라우저를 제어하고,
 * Turnstile CAPTCHA가 자동으로 해결된 후 생성된 토큰을 추출합니다.
 *
 * dev 프로파일에서만 활성화됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapinstaTokenExtractor {

  private static final String SNAPINSTA_URL = "https://snapinsta.to/ko";

  /**
   * Python undetected-chromedriver를 사용하여 snapinsta.to에서 cftoken 자동 추출
   *
   * @return Cloudflare Turnstile cftoken
   * @throws RuntimeException cftoken 추출 실패 시
   */
  public String extractCfToken() {
    lineLog("========== cftoken 자동 추출 시작 ==========");

    try {
      String token = extractTokenWithPython();

      if (token != null && !token.isEmpty()) {
        lineLog("✅ cftoken 추출 성공!");
        lineLog("토큰 길이: " + token.length() + " 문자");
        lineLog("토큰 미리보기: " + token.substring(0, Math.min(50, token.length())) + "...");
        return token;
      }

      lineLog("⚠️ 토큰 추출 실패");
      return null;

    } catch (Exception e) {
      log.error("cftoken 추출 실패", e);
      throw new RuntimeException("Turnstile 토큰 추출 실패", e);
    }
  }

  /**
   * Python undetected-chromedriver로 cftoken 추출
   * Cloudflare Turnstile 봇 감지를 성공적으로 우회
   *
   * @return cftoken 또는 null
   */
  private String extractTokenWithPython() {
    lineLog("Python 스크립트 실행 중...");

    Process process = null;
    try {
      // resources/scripts/turnstile_token_extractor.py 경로 가져오기
      ClassPathResource scriptResource = new ClassPathResource("scripts/turnstile_token_extractor.py");
      String scriptPath = scriptResource.getFile().getAbsolutePath();

      lineLog("Python 스크립트 경로: " + scriptPath);

      // ProcessBuilder로 Python 실행
      ProcessBuilder pb = new ProcessBuilder("python3", scriptPath);
      pb.redirectErrorStream(true);

      process = pb.start();

      // 출력 읽기
      StringBuilder output = new StringBuilder();

      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          lineLog("  [Python] " + line);
          output.append(line).append("\n");
        }
      }

      // 프로세스 완료 대기 (최대 60초)
      boolean finished = process.waitFor(60, TimeUnit.SECONDS);

      if (!finished) {
        lineLog("⚠️ Python 스크립트 타임아웃 (60초 초과)");
        process.destroyForcibly();
        return null;
      }

      int exitCode = process.exitValue();
      lineLog("Python 프로세스 종료 코드: " + exitCode);

      if (exitCode != 0) {
        lineLog("⚠️ Python 스크립트 실행 실패 (exit code: " + exitCode + ")");
        return null;
      }

      // 출력에서 토큰 추출 (마지막 줄이 토큰)
      String[] lines = output.toString().trim().split("\n");
      if (lines.length > 0) {
        String token = lines[lines.length - 1].trim();
        if (token.length() > 100) { // cftoken은 일반적으로 매우 긺
          lineLog("✅ Python에서 토큰 추출 성공!");
          return token;
        }
      }

      lineLog("⚠️ Python 출력에서 유효한 토큰을 찾을 수 없음");
      return null;

    } catch (Exception e) {
      lineLog("❌ Python 실행 중 에러: " + e.getMessage());
      log.error("Python 스크립트 실행 실패", e);
      return null;

    } finally {
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
    }
  }

}
