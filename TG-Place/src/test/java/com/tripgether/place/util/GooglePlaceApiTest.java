package com.tripgether.place.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripgether.common.util.NetworkUtil;
import com.tripgether.place.dto.GooglePlaceSearchDto;
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
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = TripgetherApplication.class)
@ActiveProfiles("dev")
@Slf4j
class GooglePlaceApiTest {

  @Autowired
  NetworkUtil networkUtil;

  @Autowired
  OkHttpClient okHttpClient;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${place.google.api-key}")
  private String googleApiKey;

  @Test
  public void mainTest() {
    lineLog("테스트시작");

//    timeLog(this::test);
//    timeLog(this::googleApiTest1_Basic);
//    timeLog(this::googleApiTest2_WithRegion);
//    timeLog(this::googleApiTest3_WithLocationBias);
//    timeLog(this::googleApiTest4_RegionAndLocationBias);
//    timeLog(this::googleApiTest5_WithHeaders);
//    timeLog(this::googleApiTest6_HeadersAndRegion);
//    timeLog(this::googleApiTest7_HeadersAndBias);
//    timeLog(this::googleApiTest8_AllCombined);
//    timeLog(this::googleApiTest9_NewAPI_Basic);
//    timeLog(this::googleApiTest10_NewAPI_WithLocationBias);

    // OkHttp 테스트
    timeLog(this::googleApiTest11_OkHttp_Basic);
    timeLog(this::googleApiTest12_OkHttp_WithRegion);
    timeLog(this::googleApiTest13_OkHttp_WithLocationBias);
    timeLog(this::googleApiTest14_OkHttp_AllCombined);

    lineLog("테스트종료");
  }

  public void test(){
    lineLog("이렇게 사용하는겁니다");
  }

  /**
   * 테스트 1: 기본 (location 파라미터 없음)
   * 브라우저에서는 OK, 서버에서는 ZERO_RESULTS 예상
   */
  public void googleApiTest1_Basic(){
    lineLog("[테스트 1] 기본 (location 파라미터 없음)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&key=%s",
      encodedQuery,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    logApiResponse(url);
  }

  /**
   * 테스트 2: region=kr 파라미터만 추가
   * 한국 기준 검색 (IP 무관)
   */
  public void googleApiTest2_WithRegion(){
    lineLog("[테스트 2] region=kr 추가");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&key=%s",
      encodedQuery,
      region,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    logApiResponse(url);
  }

  /**
   * 테스트 3: locationbias만 추가
   * 서울 중심 50km 반경 우선순위
   */
  public void googleApiTest3_WithLocationBias(){
    lineLog("[테스트 3] locationbias 추가 (서울 중심)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String locationBias = "circle:50000@37.5665,126.9780";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&locationbias=%s&key=%s",
      encodedQuery,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Location Bias: " + locationBias);
    logApiResponse(url);
  }

  /**
   * 테스트 4: region + locationbias 조합 (권장)
   * 가장 확실한 한국 검색
   */
  public void googleApiTest4_RegionAndLocationBias(){
    lineLog("[테스트 4] region + locationbias 조합 (권장)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";
    String locationBias = "circle:50000@37.5665,126.9780";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&locationbias=%s&key=%s",
      encodedQuery,
      region,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    lineLog("Location Bias: " + locationBias);
    logApiResponse(url);
  }

  /**
   * 테스트 5: 브라우저 헤더만 추가 (location 파라미터 없음)
   * 헤더의 영향 확인
   */
  public void googleApiTest5_WithHeaders(){
    lineLog("[테스트 5] 브라우저 헤더 추가 (location 파라미터 없음)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

    Map<String, String> headers = buildBrowserHeaders();

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&key=%s",
      encodedQuery,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("헤더: User-Agent, Accept-Language, Accept 추가");
    logApiResponseWithHeaders(url, headers);
  }

  /**
   * 테스트 6: 헤더 + region=kr
   * 헤더와 region 파라미터 조합
   */
  public void googleApiTest6_HeadersAndRegion(){
    lineLog("[테스트 6] 헤더 + region=kr");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";

    Map<String, String> headers = buildBrowserHeaders();

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&key=%s",
      encodedQuery,
      region,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    lineLog("헤더: User-Agent, Accept-Language, Accept 추가");
    logApiResponseWithHeaders(url, headers);
  }

  /**
   * 테스트 7: 헤더 + locationbias
   * 헤더와 locationbias 파라미터 조합
   */
  public void googleApiTest7_HeadersAndBias(){
    lineLog("[테스트 7] 헤더 + locationbias");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String locationBias = "circle:50000@37.5665,126.9780";

    Map<String, String> headers = buildBrowserHeaders();

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&locationbias=%s&key=%s",
      encodedQuery,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Location Bias: " + locationBias);
    lineLog("헤더: User-Agent, Accept-Language, Accept 추가");
    logApiResponseWithHeaders(url, headers);
  }

  /**
   * 테스트 8: 헤더 + region + locationbias (전체 조합)
   * 브라우저와 동일한 환경 재현
   */
  public void googleApiTest8_AllCombined(){
    lineLog("[테스트 8] 헤더 + region + locationbias (전체 조합)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";
    String locationBias = "circle:50000@37.5665,126.9780";

    Map<String, String> headers = buildBrowserHeaders();

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&locationbias=%s&key=%s",
      encodedQuery,
      region,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    lineLog("Location Bias: " + locationBias);
    lineLog("헤더: User-Agent, Accept-Language, Accept 추가");
    logApiResponseWithHeaders(url, headers);
  }

  /**
   * 브라우저 헤더 구성 (공통 메서드)
   */
  private Map<String, String> buildBrowserHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    headers.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("Accept", "application/json");
    return headers;
  }

  /**
   * API 호출 및 응답 로깅 공통 메서드 (헤더 없음)
   */
  private void logApiResponse(String url) {
    try {
      GooglePlaceSearchDto response = networkUtil.sendGetRequest(
        url,
        null,
        GooglePlaceSearchDto.class
      );

      lineLog("응답 상태: " + response.getStatus());

      if ("OK".equals(response.getStatus()) &&
          response.getCandidates() != null &&
          !response.getCandidates().isEmpty()) {

        GooglePlaceSearchDto.Candidate candidate = response.getCandidates().get(0);
        lineLog("✅ 성공 - Place ID: " + candidate.getPlaceId());
        lineLog("이름: " + candidate.getName());
        lineLog("주소: " + candidate.getFormattedAddress());

        if (candidate.getGeometry() != null && candidate.getGeometry().getLocation() != null) {
          lineLog("위도: " + candidate.getGeometry().getLocation().getLat());
          lineLog("경도: " + candidate.getGeometry().getLocation().getLng());
        }
      } else {
        lineLog("❌ 실패 - 검색 결과 없음 (ZERO_RESULTS)");
      }
    } catch (Exception e) {
      lineLog("❌ 에러 발생: " + e.getMessage());
    }
  }

  /**
   * API 호출 및 응답 로깅 공통 메서드 (헤더 포함)
   */
  private void logApiResponseWithHeaders(String url, Map<String, String> headers) {
    try {
      GooglePlaceSearchDto response = networkUtil.sendGetRequest(
        url,
        headers,
        GooglePlaceSearchDto.class
      );

      lineLog("응답 상태: " + response.getStatus());

      if ("OK".equals(response.getStatus()) &&
          response.getCandidates() != null &&
          !response.getCandidates().isEmpty()) {

        GooglePlaceSearchDto.Candidate candidate = response.getCandidates().get(0);
        lineLog("✅ 성공 - Place ID: " + candidate.getPlaceId());
        lineLog("이름: " + candidate.getName());
        lineLog("주소: " + candidate.getFormattedAddress());

        if (candidate.getGeometry() != null && candidate.getGeometry().getLocation() != null) {
          lineLog("위도: " + candidate.getGeometry().getLocation().getLat());
          lineLog("경도: " + candidate.getGeometry().getLocation().getLng());
        }
      } else {
        lineLog("❌ 실패 - 검색 결과 없음 (ZERO_RESULTS)");
      }
    } catch (Exception e) {
      lineLog("❌ 에러 발생: " + e.getMessage());
    }
  }

  /**
   * 테스트 9: Google Places API (New) - Text Search 기본
   * POST 방식, JSON body
   */
  public void googleApiTest9_NewAPI_Basic(){
    lineLog("[테스트 9] Google Places API (New) - Text Search 기본");

    String query = "코엑스";

    // 신버전 API URL (POST 방식)
    String url = String.format(
      "https://places.googleapis.com/v1/places:searchText?key=%s",
      googleApiKey
    );

    // Request Body 구성
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("textQuery", query);
    requestBody.put("languageCode", "ko");

    lineLog("검색 키워드: " + query);
    lineLog("API 방식: POST (신버전)");
    logNewApiResponse(url, requestBody);
  }

  /**
   * 테스트 10: Google Places API (New) - locationBias 추가
   * POST 방식, JSON body, 서울 중심 50km 반경
   */
  public void googleApiTest10_NewAPI_WithLocationBias(){
    lineLog("[테스트 10] Google Places API (New) - locationBias 추가");

    String query = "코엑스";

    // 신버전 API URL (POST 방식)
    String url = String.format(
      "https://places.googleapis.com/v1/places:searchText?key=%s",
      googleApiKey
    );

    // Request Body 구성
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("textQuery", query);
    requestBody.put("languageCode", "ko");

    // locationBias 추가 (서울 중심, 50km 반경)
    Map<String, Object> circle = new HashMap<>();
    Map<String, Double> center = new HashMap<>();
    center.put("latitude", 37.5665);
    center.put("longitude", 126.9780);
    circle.put("center", center);
    circle.put("radius", 50000.0);

    Map<String, Object> locationBias = new HashMap<>();
    locationBias.put("circle", circle);
    requestBody.put("locationBias", locationBias);

    lineLog("검색 키워드: " + query);
    lineLog("API 방식: POST (신버전)");
    lineLog("Location Bias: 서울 중심 50km 반경");
    logNewApiResponse(url, requestBody);
  }

  /**
   * 신버전 API 응답 로깅 (POST 방식)
   */
  private void logNewApiResponse(String url, Map<String, Object> requestBody) {
    try {
      // POST 방식으로 호출
      GooglePlaceSearchDto response = networkUtil.sendPostRequest(
        url,
        requestBody,
        null,
        GooglePlaceSearchDto.class
      );

      lineLog("응답 상태: " + response.getStatus());

      if ("OK".equals(response.getStatus()) &&
          response.getCandidates() != null &&
          !response.getCandidates().isEmpty()) {

        GooglePlaceSearchDto.Candidate candidate = response.getCandidates().get(0);
        lineLog("✅ 성공 - Place ID: " + candidate.getPlaceId());
        lineLog("이름: " + candidate.getName());
        lineLog("주소: " + candidate.getFormattedAddress());

        if (candidate.getGeometry() != null && candidate.getGeometry().getLocation() != null) {
          lineLog("위도: " + candidate.getGeometry().getLocation().getLat());
          lineLog("경도: " + candidate.getGeometry().getLocation().getLng());
        }
      } else {
        lineLog("❌ 실패 - 검색 결과 없음 또는 다른 상태");
      }
    } catch (Exception e) {
      lineLog("❌ 에러 발생: " + e.getMessage());
      log.error("신버전 API 호출 실패", e);
    }
  }

  // ========================================
  // OkHttp 테스트 (구버전 API)
  // ========================================

  /**
   * 테스트 11: OkHttp - 기본 (브라우저 헤더 포함)
   * CookieJar를 통한 자동 쿠키 관리 + 브라우저 헤더
   */
  public void googleApiTest11_OkHttp_Basic(){
    lineLog("[테스트 11] OkHttp - 기본 (브라우저 헤더 포함)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&key=%s",
      encodedQuery,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("HTTP 클라이언트: OkHttp + CookieJar");
    logOkHttpResponse(url);
  }

  /**
   * 테스트 12: OkHttp - region=kr 추가
   * 브라우저 헤더 + region 파라미터
   */
  public void googleApiTest12_OkHttp_WithRegion(){
    lineLog("[테스트 12] OkHttp - region=kr 추가");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&key=%s",
      encodedQuery,
      region,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    lineLog("HTTP 클라이언트: OkHttp + CookieJar");
    logOkHttpResponse(url);
  }

  /**
   * 테스트 13: OkHttp - locationbias 추가
   * 브라우저 헤더 + 서울 중심 50km 반경
   */
  public void googleApiTest13_OkHttp_WithLocationBias(){
    lineLog("[테스트 13] OkHttp - locationbias 추가 (서울 중심)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String locationBias = "circle:50000@37.5665,126.9780";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&locationbias=%s&key=%s",
      encodedQuery,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Location Bias: " + locationBias);
    lineLog("HTTP 클라이언트: OkHttp + CookieJar");
    logOkHttpResponse(url);
  }

  /**
   * 테스트 14: OkHttp - 전체 조합 (region + locationbias)
   * 브라우저 완전 재현
   */
  public void googleApiTest14_OkHttp_AllCombined(){
    lineLog("[테스트 14] OkHttp - 전체 조합 (region + locationbias)");

    String query = "코엑스";
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    String region = "kr";
    String locationBias = "circle:50000@37.5665,126.9780";

    String url = String.format(
      "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total&language=ko&region=%s&locationbias=%s&key=%s",
      encodedQuery,
      region,
      locationBias,
      googleApiKey
    );

    lineLog("검색 키워드: " + query);
    lineLog("Region: " + region);
    lineLog("Location Bias: " + locationBias);
    lineLog("HTTP 클라이언트: OkHttp + CookieJar");
    logOkHttpResponse(url);
  }

  /**
   * OkHttp를 사용한 API 호출 및 응답 로깅
   * 브라우저 헤더 + CookieJar 자동 쿠키 관리
   */
  private void logOkHttpResponse(String url) {
    try {
      // 브라우저 헤더 구성
      Request request = new Request.Builder()
        .url(url)
        .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
        .addHeader("Accept", "application/json")
        .addHeader("Referer", "https://developers.google.com/")
        .get()
        .build();

      // OkHttp로 요청 실행 (CookieJar 자동 동작)
      try (Response response = okHttpClient.newCall(request).execute()) {
        lineLog("HTTP 상태 코드: " + response.code());

        if (response.isSuccessful() && response.body() != null) {
          String responseBody = response.body().string();

          // JSON 파싱
          GooglePlaceSearchDto dto = objectMapper.readValue(responseBody, GooglePlaceSearchDto.class);
          lineLog("응답 상태: " + dto.getStatus());

          if ("OK".equals(dto.getStatus()) &&
              dto.getCandidates() != null &&
              !dto.getCandidates().isEmpty()) {

            GooglePlaceSearchDto.Candidate candidate = dto.getCandidates().get(0);
            lineLog("✅ 성공 - Place ID: " + candidate.getPlaceId());
            lineLog("이름: " + candidate.getName());
            lineLog("주소: " + candidate.getFormattedAddress());

            if (candidate.getGeometry() != null && candidate.getGeometry().getLocation() != null) {
              lineLog("위도: " + candidate.getGeometry().getLocation().getLat());
              lineLog("경도: " + candidate.getGeometry().getLocation().getLng());
            }
          } else {
            lineLog("❌ 실패 - 검색 결과 없음 (ZERO_RESULTS)");
            lineLog("응답 본문: " + responseBody);
          }
        } else {
          lineLog("❌ HTTP 요청 실패 - 상태 코드: " + response.code());
        }
      }
    } catch (Exception e) {
      lineLog("❌ 에러 발생: " + e.getMessage());
      log.error("OkHttp API 호출 실패", e);
    }
  }

}
