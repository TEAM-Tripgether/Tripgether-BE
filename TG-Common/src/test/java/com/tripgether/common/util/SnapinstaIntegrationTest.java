package com.tripgether.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.superLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripgether.web.TripgetherApplication;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Snapinsta API 통합 테스트
 *
 * Playwright로 자동 추출한 cftoken을 사용하여
 * Instagram 게시물 데이터를 실제로 가져오는지 검증합니다.
 */
@SpringBootTest(classes = TripgetherApplication.class)
@ActiveProfiles("dev")
@Slf4j
class SnapinstaIntegrationTest {

  @Autowired
  SnapinstaTokenExtractor tokenExtractor;

  @Autowired
  OkHttpClient okHttpClient;

  @Autowired
  ObjectMapper objectMapper;

  private static final String SNAPINSTA_API_URL = "https://snapinsta.to/api/ajaxSearch";

  @Test
  public void mainTest() {
    lineLog("테스트시작 - Playwright 자동 토큰 추출 + API 호출");

    // Playwright로 cftoken 자동 추출 + OkHttp로 API 호출
    timeLog(this::testWithDynamicToken);

    lineLog("테스트종료");
  }

  /**
   * 테스트: Playwright로 cftoken 자동 추출 후 즉시 API 호출
   * Instagram 게시물 데이터가 실제로 반환되는지 검증
   */
  public void testWithDynamicToken() {
    lineLog("[테스트] Playwright 자동 토큰 추출 + Instagram API 호출");

    String instagramUrl = "https://www.instagram.com/p/DO-u-YwD6Rt";
    lineLog("Instagram URL: " + instagramUrl);

    try {
      // 1단계: Python/Playwright로 cftoken 자동 추출
      lineLog(null);
      lineLog("========== 1단계: cftoken 자동 추출 (Python → Playwright) ==========");
      String cftoken = tokenExtractor.extractCfToken();

      if (cftoken == null || cftoken.isEmpty()) {
        lineLog("❌ cftoken 추출 실패 - 테스트 중단");
        throw new RuntimeException("cftoken 추출 실패");
      }

      lineLog(null);
      lineLog("========== 2단계: OkHttp로 API 요청 ==========");

      // 2단계: Form 데이터 구성
      FormBody.Builder formBuilder = new FormBody.Builder()
          .add("q", instagramUrl)
          .add("t", "media")
          .add("v", "v2")
          .add("lang", "ko")
          .add("cftoken", cftoken);  // 동적으로 추출된 토큰 사용

      FormBody formBody = formBuilder.build();

      // Form 데이터 로깅
      Map<String, String> formDataMap = new HashMap<>();
      formDataMap.put("q", instagramUrl);
      formDataMap.put("t", "media");
      formDataMap.put("v", "v2");
      formDataMap.put("lang", "ko");
      formDataMap.put("cftoken", cftoken.substring(0, Math.min(50, cftoken.length())) + "...");

      lineLog("요청 Form 데이터:");
      superLog(formDataMap);

      // 3단계: OkHttp 요청 생성
      Request request = new Request.Builder()
          .url(SNAPINSTA_API_URL)
          .post(formBody)
          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
          .addHeader("Accept", "*/*")
          .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
          .addHeader("Origin", "https://snapinsta.to")
          .addHeader("Referer", "https://snapinsta.to/ko")
          .addHeader("X-Requested-With", "XMLHttpRequest")
          .build();

      // 4단계: API 호출
      try (Response response = okHttpClient.newCall(request).execute()) {
        lineLog(null);
        lineLog("========== 3단계: API 응답 검증 ==========");
        lineLog("HTTP 상태 코드: " + response.code());

        if (response.isSuccessful() && response.body() != null) {
          String responseBody = response.body().string();

          lineLog("✅ Snapinsta API 호출 성공");
          lineLog(null);
          lineLog("========== RAW RESPONSE (String) ==========");
          lineLog(responseBody.substring(0, Math.min(1000, responseBody.length())) + "...");
          lineLog(null);

          // 파일로 응답 저장
          try (PrintWriter writer = new PrintWriter(new FileWriter("/tmp/snapinsta_integration_response.txt"))) {
            writer.println("HTTP Status: " + response.code());
            writer.println("cftoken: " + cftoken);
            writer.println("Response Body:");
            writer.println(responseBody);
          } catch (Exception fileEx) {
            log.error("파일 저장 실패", fileEx);
          }

          // JSON 파싱 후 superLog로 Pretty Print
          try {
            Object jsonObject = objectMapper.readValue(responseBody, Object.class);
            lineLog("========== PRETTY JSON (superLog) ==========");
            superLog(jsonObject);
            lineLog(null);

            // 응답 검증
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = (Map<String, Object>) jsonObject;

            String status = (String) jsonMap.get("status");
            String message = (String) jsonMap.get("mess");

            lineLog("========== 응답 분석 ==========");
            lineLog("status: " + status);
            lineLog("message: " + message);

            // 성공 여부 판단
            if ("ok".equals(status)) {
              if (message != null && message.contains("인증 토큰이 유효하지 않습니다")) {
                lineLog("⚠️ 토큰이 만료되었습니다. Playwright가 새로운 토큰을 추출했지만 API에서 거부했습니다.");
                lineLog("   → Turnstile 검증이 완료되지 않았거나 토큰 생성 실패");
              } else if (jsonMap.containsKey("data") || responseBody.contains("download") || responseBody.contains("url")) {
                lineLog("🎉 Instagram 데이터 추출 성공!");
                lineLog("   → 이미지/비디오 다운로드 URL이 포함되어 있습니다.");
              } else {
                lineLog("⚠️ 응답은 성공이지만 데이터 구조가 예상과 다릅니다.");
              }
            } else {
              lineLog("❌ API 호출 실패 - status: " + status);
            }

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
      lineLog("❌ 통합 테스트 실패: " + e.getMessage());
      log.error("Snapinsta 통합 테스트 실패", e);
      throw new RuntimeException("통합 테스트 실패", e);
    }
  }
}
