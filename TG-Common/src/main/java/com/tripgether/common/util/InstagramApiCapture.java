package com.tripgether.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Instagram API 응답 캡처 유틸리티 클래스
 * Selenium WebDriver를 사용하여 sssinstagram.com의 API 응답을 캡처합니다.
 * Lazy 초기화를 통해 실제 사용 시점에만 WebDriver를 생성합니다.
 */
@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class InstagramApiCapture {

  private final WebDriver webDriver;
  private final Gson gson;

  private static final String SSS_INSTAGRAM_URL = "https://sssinstagram.com";
  private static final int MAX_WAIT_SECONDS = 60;
  private static final int POLL_INTERVAL_MS = 1000;

  /**
   * XHR 후킹 스크립트
   * XMLHttpRequest와 Fetch API를 가로채서 /api/convert 응답을 캡처합니다.
   */
  private static final String XHR_HOOK_SCRIPT = """
      (function() {
          window.capturedApiResponse = null;

          // XMLHttpRequest 후킹
          const originalOpen = XMLHttpRequest.prototype.open;
          const originalSend = XMLHttpRequest.prototype.send;

          XMLHttpRequest.prototype.open = function(method, url) {
              this._url = url;
              this._method = method;
              return originalOpen.apply(this, arguments);
          };

          XMLHttpRequest.prototype.send = function(body) {
              const xhr = this;

              xhr.addEventListener('load', function() {
                  if (xhr._url && xhr._url.includes('/api/convert')) {
                      console.log('🎯 API 응답 캡처!');

                      window.capturedApiResponse = {
                          url: xhr._url,
                          status: xhr.status,
                          response: xhr.responseText
                      };
                  }
              });

              return originalSend.apply(this, arguments);
          };

          // Fetch API 후킹
          const originalFetch = window.fetch;
          window.fetch = async function(...args) {
              const response = await originalFetch.apply(this, args);

              const url = typeof args[0] === 'string' ? args[0] : args[0].url;

              if (url.includes('/api/convert')) {
                  const clonedResponse = response.clone();
                  const text = await clonedResponse.text();

                  window.capturedApiResponse = {
                      url: url,
                      status: response.status,
                      response: text
                  };
              }

              return response;
          };

          console.log('✅ XHR/Fetch 후킹 완료!');
      })();
      """;

  /**
   * Instagram URL로부터 API 응답을 캡처합니다.
   *
   * @param instagramUrl Instagram 게시물 URL
   * @return API 응답 JSON
   * @throws CustomException Instagram URL이 유효하지 않거나 API 응답 캡처 실패 시
   */
  public JsonArray captureApiResponse(String instagramUrl) {
    validateInstagramUrl(instagramUrl);

    try {
      log.info("Instagram API 응답 캡처 시작: {}", instagramUrl);

      // Step 1: sssinstagram.com 로드
      webDriver.get(SSS_INSTAGRAM_URL);
      log.debug("페이지 로드 완료");

      // Step 2: XHR 후킹 스크립트 주입
      JavascriptExecutor js = (JavascriptExecutor) webDriver;
      js.executeScript(XHR_HOOK_SCRIPT);
      log.debug("XHR 후킹 완료");

      // Step 3: Instagram URL 입력
      WebElement urlInput = webDriver.findElement(
          By.cssSelector("input[type='text'], input[type='url'], input.form__input")
      );
      urlInput.clear();
      urlInput.sendKeys(instagramUrl);
      log.debug("URL 입력 완료");

      // 입력 처리 대기
      Thread.sleep(500);

      // Step 4: 다운로드 버튼 클릭
      WebElement downloadButton = webDriver.findElement(
          By.cssSelector("button.form__submit[type='submit']")
      );
      js.executeScript("arguments[0].click();", downloadButton);
      log.debug("다운로드 버튼 클릭 완료");

      // Step 5: API 응답 대기 및 추출
      String apiResponse = waitForApiResponse(js);

      if (apiResponse == null || apiResponse.isEmpty()) {
        throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
      }

      log.info("API 응답 캡처 성공");

      // JSON 파싱
      JsonArray jsonResponse = JsonParser.parseString(apiResponse).getAsJsonArray();
      return jsonResponse;

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Instagram API 캡처 중 인터럽트 발생", e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error("Instagram API 응답 캡처 실패: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
    }
  }

  /**
   * API 응답이 캡처될 때까지 대기합니다.
   *
   * @param js JavascriptExecutor
   * @return 캡처된 API 응답
   */
  private String waitForApiResponse(JavascriptExecutor js) throws InterruptedException {
    for (int i = 0; i < MAX_WAIT_SECONDS; i++) {
      Thread.sleep(POLL_INTERVAL_MS);

      Object captured = js.executeScript("return window.capturedApiResponse;");
      if (captured != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) captured;

        Long status = (Long) responseMap.get("status");
        String response = (String) responseMap.get("response");

        if (status == 200 && response != null && !response.isEmpty()) {
          log.debug("API 응답 캡처됨 ({}초)", i + 1);
          return response;
        }
      }
    }

    log.error("API 응답 캡처 타임아웃 ({}초)", MAX_WAIT_SECONDS);
    return null;
  }

  /**
   * Instagram URL 유효성 검증
   *
   * @param url 검증할 URL
   * @throws CustomException URL이 유효하지 않을 경우
   */
  private void validateInstagramUrl(String url) {
    if (url == null || url.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }

    if (!url.startsWith("https://www.instagram.com/") && !url.startsWith("http://www.instagram.com/")) {
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }
}
