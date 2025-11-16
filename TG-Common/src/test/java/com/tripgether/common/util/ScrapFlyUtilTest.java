package com.tripgether.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripgether.web.TripgetherApplication;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@SpringBootTest(classes = TripgetherApplication.class)
@ActiveProfiles("dev")
@Slf4j
class ScrapFlyUtilTest {

  @Autowired
  ScrapFlyUtil scrapFlyUtil;

  @Autowired
  OkHttpClient okHttpClient;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${scrapfly.api-key}")
  private String scrapflyApiKey;

  private static final String SCRAPFLY_API_URL = "https://api.scrapfly.io/scrape";

  @Test
  public void mainTest() {
    lineLog("테스트시작");

//    timeLog(this::testScrapfly1_BasicConnection);
//    timeLog(this::testScrapfly2_HttpBinTest);
//    timeLog(this::testScrapfly3_ExampleDotCom);
//    timeLog(this::testScrapfly4_InstagramProfile);
    timeLog(this::testScrapfly5_InstagramPostUrl);

    lineLog("테스트종료");
  }

  /**
   * 테스트 1: Scrapfly API 기본 연결 확인
   * 단순 URL로 API 응답 검증
   */
  public void testScrapfly1_BasicConnection() {
    lineLog("[테스트 1] Scrapfly API 기본 연결 확인");

    String targetUrl = "https://www.instagram.com/p/DO-u-YwD6Rt";
    String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

    String apiUrl = String.format(
        "%s?url=%s&key=%s",
        SCRAPFLY_API_URL,
        encodedUrl,
        scrapflyApiKey
    );

    lineLog("요청 URL: " + targetUrl);
    lineLog("Scrapfly API Endpoint: " + SCRAPFLY_API_URL);

    executeScrapflyRequest(apiUrl);
  }

  /**
   * 테스트 2: HttpBin 테스트 (JSON 응답)
   * JSON 응답을 받아서 Pretty Print
   */
  public void testScrapfly2_HttpBinTest() {
    lineLog("[테스트 2] HttpBin JSON 응답 테스트");

    String targetUrl = "https://httpbin.org/json";
    String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

    String apiUrl = String.format(
        "%s?url=%s&key=%s",
        SCRAPFLY_API_URL,
        encodedUrl,
        scrapflyApiKey
    );

    lineLog("요청 URL: " + targetUrl);

    executeScrapflyRequest(apiUrl);
  }

  /**
   * 테스트 3: Example.com 스크래핑
   * 정적 HTML 페이지 스크래핑
   */
  public void testScrapfly3_ExampleDotCom() {
    lineLog("[테스트 3] Example.com 정적 페이지 스크래핑");

    String targetUrl = "https://example.com";
    String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

    String apiUrl = String.format(
        "%s?url=%s&key=%s",
        SCRAPFLY_API_URL,
        encodedUrl,
        scrapflyApiKey
    );

    lineLog("요청 URL: " + targetUrl);

    executeScrapflyRequest(apiUrl);
  }

  /**
   * 테스트 4: Instagram 프로필 스크래핑
   * Instagram web_profile_info API를 통한 프로필 정보 추출
   */
  public void testScrapfly4_InstagramProfile() {
    lineLog("[테스트 4] Instagram 프로필 스크래핑");

    String username = "google";
    String targetUrl = String.format(
        "https://i.instagram.com/api/v1/users/web_profile_info/?username=%s",
        username
    );
    String encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);

    String apiUrl = String.format(
        "%s?url=%s&key=%s",
        SCRAPFLY_API_URL,
        encodedUrl,
        scrapflyApiKey
    );

    lineLog("요청 URL: " + targetUrl);
    lineLog("Instagram 사용자: " + username);

    executeScrapflyRequest(apiUrl);
  }

  /**
   * 테스트 5: Instagram 게시물 URL 스크래핑
   * Instagram 게시물 페이지 데이터 추출
   */
  public void testScrapfly5_InstagramPostUrl() {
    lineLog("[테스트 5] Instagram 게시물 URL 스크래핑");

    String postUrl = "https://www.instagram.com/instagram/p/DRGsYMLjLFp/";
    String encodedUrl = URLEncoder.encode(postUrl, StandardCharsets.UTF_8);

    String apiUrl = String.format(
        "%s?url=%s&key=%s&render_js=%s&asp=%s",
        SCRAPFLY_API_URL,
        encodedUrl,
        scrapflyApiKey,
        "true",  // JavaScript 렌더링 활성화 (Instagram SPA 필수)
        "true"   // Anti-Scraping Protection 우회 (Instagram Bot 탐지 우회)
    );

    lineLog("게시물 URL: " + postUrl);
    lineLog("Post ID: DO-u-YwD6Rt");
    lineLog("파라미터: render_js=true, asp=true");

    executeScrapflyRequest(apiUrl);
  }

  /**
   * Scrapfly API 요청 실행 및 응답 출력 (공통 메서드)
   *
   * @param apiUrl Scrapfly API 전체 URL
   */
  private void executeScrapflyRequest(String apiUrl) {
    try {
      // OkHttp 요청 생성 (브라우저 헤더 포함)
      Request request = new Request.Builder()
          .url(apiUrl)
          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .addHeader("Accept", "application/json")
          .get()
          .build();

      // API 호출
      try (Response response = okHttpClient.newCall(request).execute()) {
        lineLog("HTTP 상태 코드: " + response.code());

        if (response.isSuccessful() && response.body() != null) {
          String responseBody = response.body().string();

          lineLog("✅ Scrapfly API 호출 성공");
          lineLog(null);
          lineLog("========== RAW RESPONSE (String) ==========");
          lineLog(responseBody.substring(0, Math.min(500, responseBody.length())) + "...");
          lineLog(null);

          // JSON 파싱 후 superLog로 Pretty Print
          try {
            Object jsonObject = objectMapper.readValue(responseBody, Object.class);
            lineLog("========== PRETTY JSON (superLog) ==========");
            superLog(jsonObject);
            lineLog(null);
          } catch (Exception parseException) {
            lineLog("⚠️ JSON 파싱 실패 - Raw String으로만 출력");
            log.warn("JSON 파싱 실패", parseException);
          }

        } else {
          lineLog("❌ HTTP 요청 실패 - 상태 코드: " + response.code());
          if (response.body() != null) {
            lineLog("에러 응답: " + response.body().string());
          }
        }
      }
    } catch (Exception e) {
      lineLog("❌ 에러 발생: " + e.getMessage());
      log.error("Scrapfly API 호출 실패", e);
    }
  }

}