package com.tripgether.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
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
   * Python undetected-chromedriver 또는 Playwright를 사용하여 snapinsta.to에서 cftoken 자동 추출
   *
   * @return Cloudflare Turnstile cftoken
   * @throws RuntimeException cftoken 추출 실패 시
   */
  public String extractCfToken() {
    lineLog("========== cftoken 자동 추출 시작 ==========");

    try {
      // 1차 시도: Python undetected-chromedriver (봇 감지 우회 성공)
      lineLog("1차 시도: Python undetected-chromedriver");
      String token = extractTokenWithPython();

      if (token != null && !token.isEmpty()) {
        lineLog("✅ Python으로 cftoken 추출 성공!");
        lineLog("토큰 길이: " + token.length() + " 문자");
        lineLog("토큰 미리보기: " + token.substring(0, Math.min(50, token.length())) + "...");
        return token;
      }

      // 2차 시도: Playwright (Fallback)
      lineLog("⚠️ Python 추출 실패, 2차 시도: Playwright");
      token = extractTokenWithPlaywright();

      if (token != null && !token.isEmpty()) {
        lineLog("✅ Playwright로 cftoken 추출 성공!");
        lineLog("토큰 길이: " + token.length() + " 문자");
        lineLog("토큰 미리보기: " + token.substring(0, Math.min(50, token.length())) + "...");
        return token;
      }

      lineLog("⚠️ 모든 추출 방법 실패");
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

  /**
   * Playwright를 사용하여 실제 브라우저에서 토큰 추출
   */
  private String extractTokenWithPlaywright() {
    lineLog("Playwright 브라우저 시작...");

    Playwright playwright = null;
    Browser browser = null;
    Scanner scanner = null;

    try {
      playwright = Playwright.create();
      browser = playwright.chromium().launch(
          new BrowserType.LaunchOptions()
              .setHeadless(false) // 디버깅을 위해 브라우저 창 표시
              .setSlowMo(500) // 액션 슬로우 모션
              .setArgs(java.util.Arrays.asList(
                  "--disable-blink-features=AutomationControlled", // 자동화 감지 비활성화
                  "--disable-dev-shm-usage",
                  "--no-sandbox"
              ))
      );

      // User-Agent 설정 및 봇 감지 우회
      Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
          .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
          .setViewportSize(1920, 1080);

      BrowserContext context = browser.newContext(contextOptions);

      // webdriver 속성 제거 (봇 감지 우회) - delete로 먼저 제거
      context.addInitScript(
          "delete Object.getPrototypeOf(navigator).webdriver;"
      );

      Page page = context.newPage();

      lineLog("Snapinsta 페이지 열기: " + SNAPINSTA_URL);
      page.navigate(SNAPINSTA_URL);

      lineLog("페이지 로드 대기 중...");
      page.waitForLoadState();

      lineLog("Turnstile 토큰 생성 대기 중... (최대 30초)");

      // ChatGPT 제안: Turnstile input의 value가 채워질 때까지 대기
      try {
        page.waitForFunction(
            "document.querySelector('input[name=\"cf-turnstile-response\"]')?.value?.length > 0",
            new Page.WaitForFunctionOptions().setTimeout(30000)  // 30초 타임아웃
        );

        lineLog("✅ Turnstile 토큰 생성 완료!");

      } catch (TimeoutError e) {
        lineLog("⚠️ Turnstile 토큰이 30초 내에 생성되지 않았습니다.");
        lineLog("디버깅 정보 출력:");
        lineLog("  페이지 URL: " + page.url());
        lineLog("  window.turnstile 존재: " + page.evaluate("typeof window.turnstile !== 'undefined'"));
        lineLog("  Turnstile input 존재: " + page.evaluate("document.querySelector('input[name=\"cf-turnstile-response\"]') !== null"));
        lineLog("  현재 input value: " + page.evaluate("document.querySelector('input[name=\"cf-turnstile-response\"]')?.value || 'null'"));
      }

      lineLog("JavaScript로 cftoken 추출 중...");

      // ChatGPT 제안: input의 value를 직접 읽기 (더 안정적)
      Object tokenObj = page.evaluate("document.querySelector('input[name=\"cf-turnstile-response\"]')?.value");

      String token = (tokenObj != null && !tokenObj.toString().equals("null")) ? tokenObj.toString() : null;

      if (token != null && !token.isEmpty()) {
        lineLog("추출된 토큰: " + token.substring(0, Math.min(50, token.length())) + "...");
      } else {
        lineLog("⚠️ 토큰이 비어있습니다.");
        lineLog("============================================================");
        lineLog("브라우저를 열어둔 상태로 대기합니다.");
        lineLog("F12 개발자 도구 콘솔에서 다음을 실행해보세요:");
        lineLog("  window.turnstile.getResponse()");
        lineLog("  또는");
        lineLog("  document.querySelector('input[name=\"cf-turnstile-response\"]').value");
        lineLog("============================================================");
        lineLog("확인 후 엔터를 눌러 종료하세요...");

        scanner = new Scanner(System.in);
        scanner.nextLine(); // 사용자 입력 대기
      }

      lineLog("브라우저 종료");
      if (browser != null) {
        browser.close();
      }
      if (playwright != null) {
        playwright.close();
      }
      if (scanner != null) {
        scanner.close();
      }

      return token;

    } catch (Exception e) {
      // 브라우저 정리
      if (browser != null) {
        try { browser.close(); } catch (Exception ignored) {}
      }
      if (playwright != null) {
        try { playwright.close(); } catch (Exception ignored) {}
      }
      if (scanner != null) {
        try { scanner.close(); } catch (Exception ignored) {}
      }
      throw e;
    }
  }
}
