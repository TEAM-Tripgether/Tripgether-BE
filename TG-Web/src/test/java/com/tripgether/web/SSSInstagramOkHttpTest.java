package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 테스트: sssinstagram.com API 플로우 분석 테스트
 *
 * ⚠️  현재 상태: 401 SIGNATURE_HASH_MISMATCH 에러 발생 (예상된 동작)
 *
 * 📋 API 플로우 (Selenium 분석 결과):
 *    1. GET /msec → 서버 타임스탬프 조회 (성공 ✅)
 *    2. POST /api/convert → Instagram 변환 요청 (401 ❌)
 *
 * ❌ 실패 원인:
 *    - `_s` 서명 파라미터가 동적으로 JavaScript에서 생성됨
 *    - 하드코딩된 서명 값은 매 요청마다 무효화됨
 *    - 서명 알고리즘: SHA-256 HMAC 또는 커스텀 해시 (추정)
 *
 * 📊 Selenium 테스트 비교 결과:
 *    - OkHttp 서명:  54dfb4ab7dc165e718702c5e5772ab930afd7a1cfd127af9b020f563f6d83ba9 → 401 ❌
 *    - Selenium 서명: 538c4b73b87e616dc1d4a9626d17105b1d2b5c86855583dbd9a810b0fcd92ab6 → 200 ✅
 *
 * 🔧 해결 방법:
 *    A) JavaScript 코드 역공학 후 서명 생성 알고리즘을 Java로 구현
 *    B) Selenium에서 서명을 추출하여 OkHttp 요청에 주입
 *    C) Selenium만 사용하여 브라우저 시뮬레이션 (추가 HTTP 클라이언트 불필요)
 *
 * 🎯 현재 테스트 목적:
 *    - API 플로우 이해 및 문서화 ✅
 *    - 쿠키 관리 메커니즘 검증 ✅
 *    - 요청/응답 형식 파악 ✅
 *    - 서명 필요성 확인 (예상된 401 에러) ✅
 *
 * 📖 참고 문서: TG-Web/API_FLOW_ANALYSIS.md
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SSSInstagramOkHttpTest {

    private OkHttpClient client;
    private Gson gson;

    private static final String BASE_URL = "https://sssinstagram.com";
    private static final String MSEC_ENDPOINT = "/msec";
    private static final String CONVERT_ENDPOINT = "/api/convert";
    private static final String INSTAGRAM_POST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    // 테스트 중 공유할 데이터
    private Double msecValue;
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @BeforeAll
    void setupClass() {
        System.out.println("=================================================");
        System.out.println("🚀 OkHttp 테스트 초기화: sssinstagram.com API");
        System.out.println("=================================================\n");

        // Gson 초기화
        gson = new GsonBuilder().setPrettyPrinting().create();

        // HTTP 로깅 인터셉터 설정
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
            System.out.println("📡 HTTP: " + message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        // 커스텀 CookieJar 구현 (쿠키 자동 관리)
        CookieJar cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                System.out.println("\n🍪 쿠키 저장:");
                for (Cookie cookie : cookies) {
                    System.out.println("   - " + cookie.name() + " = " + cookie.value());
                }
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                if (cookies != null && !cookies.isEmpty()) {
                    System.out.println("\n🍪 쿠키 로드:");
                    for (Cookie cookie : cookies) {
                        System.out.println("   - " + cookie.name() + " = " + cookie.value());
                    }
                }
                return cookies != null ? cookies : new ArrayList<>();
            }
        };

        // OkHttpClient 초기화
        client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        System.out.println("✅ OkHttpClient 초기화 완료");
        System.out.println("✅ CookieJar 설정 완료 (자동 쿠키 관리)");
        System.out.println("✅ HTTP 로깅 인터셉터 설정 완료\n");
    }

    @Test
    @Order(1)
    @DisplayName("Step 1: GET /msec - msec 값 조회 테스트")
    void testGetMsec() throws IOException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 1: GET /msec API 호출");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 요청 URL
        String url = BASE_URL + MSEC_ENDPOINT;
        System.out.println("🌐 요청 URL: " + url);
        System.out.println("📤 요청 메서드: GET");

        // 요청 빌드
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "*/*")
                .addHeader("accept-encoding", "gzip, deflate, br, zstd")
                .addHeader("accept-language", "en-US,en;q=0.9,ko;q=0.8")
                .addHeader("cache-control", "no-cache")
                .addHeader("pragma", "no-cache")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .build();

        System.out.println("\n📋 요청 헤더:");
        request.headers().forEach(header ->
            System.out.println("   " + header.getFirst() + ": " + header.getSecond())
        );

        // 요청 실행
        System.out.println("\n⏳ 요청 전송 중...\n");

        try (Response response = client.newCall(request).execute()) {
            // 응답 상태 로깅
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📥 응답 수신");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("✅ 상태 코드: " + response.code() + " " + response.message());
            System.out.println("🌐 프로토콜: " + response.protocol());

            // 응답 헤더 로깅
            System.out.println("\n📋 응답 헤더:");
            response.headers().forEach(header ->
                System.out.println("   " + header.getFirst() + ": " + header.getSecond())
            );

            // 응답 본문 파싱
            ResponseBody responseBody = response.body();
            Assertions.assertNotNull(responseBody, "응답 본문이 null입니다");

            String responseString = responseBody.string();
            System.out.println("\n📦 응답 본문 (Raw):");
            System.out.println(responseString);

            // JSON 파싱
            JsonObject jsonResponse = gson.fromJson(responseString, JsonObject.class);
            System.out.println("\n📦 응답 본문 (JSON):");
            System.out.println(gson.toJson(jsonResponse));

            // msec 값 추출
            Assertions.assertTrue(jsonResponse.has("msec"), "응답에 msec 필드가 없습니다");
            msecValue = jsonResponse.get("msec").getAsDouble();

            System.out.println("\n🎯 추출된 msec 값: " + msecValue);
            System.out.println("✅ msec 값 저장 완료 (다음 단계에서 사용)\n");

            // 검증
            Assertions.assertEquals(200, response.code(), "응답 코드가 200이 아닙니다");
            Assertions.assertNotNull(msecValue, "msec 값이 null입니다");
            Assertions.assertTrue(msecValue > 0, "msec 값이 0보다 작거나 같습니다");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: POST /api/convert - Instagram 변환 요청 테스트 (401 예상)")
    void testPostConvert() throws IOException {
        // msec 값 확인
        Assertions.assertNotNull(msecValue, "msec 값이 없습니다. Step 1을 먼저 실행하세요.");

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📍 Step 2: POST /api/convert API 호출");
        System.out.println("⚠️  예상 결과: 401 SIGNATURE_HASH_MISMATCH (동적 서명 필요)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 요청 URL
        String url = BASE_URL + CONVERT_ENDPOINT;
        System.out.println("🌐 요청 URL: " + url);
        System.out.println("📤 요청 메서드: POST");
        System.out.println("🔗 Instagram URL: " + INSTAGRAM_POST_URL);

        // 현재 시간 (msec * 1000 + 밀리초 오프셋)
        long ts = (long) (msecValue * 1000);
        System.out.println("⏰ msec 기반 타임스탬프: " + ts);

        // 요청 페이로드 생성
        JsonObject payload = new JsonObject();
        payload.addProperty("url", INSTAGRAM_POST_URL);
        payload.addProperty("ts", ts);

        // ⚠️ 하드코딩된 서명 (실패 예상)
        // Selenium 성공 서명: 538c4b73b87e616dc1d4a9626d17105b1d2b5c86855583dbd9a810b0fcd92ab6
        // 이 서명은 JavaScript에서 동적으로 생성되므로 하드코딩 시 401 에러 발생
        payload.addProperty("_s", "54dfb4ab7dc165e718702c5e5772ab930afd7a1cfd127af9b020f563f6d83ba9");
        payload.addProperty("_ts", 1761979938888L);
        payload.addProperty("_tsc", 0);

        String jsonPayload = gson.toJson(payload);

        System.out.println("\n📦 요청 페이로드 (JSON):");
        System.out.println(gson.toJson(payload));
        System.out.println("\n⚠️  '_s' 서명은 하드코딩된 값으로 401 에러가 예상됩니다.");

        // 요청 본문 생성
        RequestBody requestBody = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
        );

        // 요청 빌드
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-encoding", "gzip, deflate, br, zstd")
                .addHeader("accept-language", "en-US,en;q=0.9,ko;q=0.8")
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/json")
                .addHeader("origin", BASE_URL)
                .addHeader("pragma", "no-cache")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .build();

        System.out.println("\n📋 요청 헤더:");
        request.headers().forEach(header ->
            System.out.println("   " + header.getFirst() + ": " + header.getSecond())
        );

        // 요청 실행
        System.out.println("\n⏳ 요청 전송 중...\n");

        try (Response response = client.newCall(request).execute()) {
            // 응답 상태 로깅
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📥 응답 수신");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("✅ 상태 코드: " + response.code() + " " + response.message());
            System.out.println("🌐 프로토콜: " + response.protocol());

            // 응답 헤더 로깅
            System.out.println("\n📋 응답 헤더:");
            response.headers().forEach(header ->
                System.out.println("   " + header.getFirst() + ": " + header.getSecond())
            );

            // 응답 본문 파싱
            ResponseBody responseBody = response.body();
            Assertions.assertNotNull(responseBody, "응답 본문이 null입니다");

            String responseString = responseBody.string();
            System.out.println("\n📦 응답 본문 (Raw):");
            System.out.println(responseString);

            // JSON 파싱 및 구조 분석
            try {
                JsonObject jsonResponse = gson.fromJson(responseString, JsonObject.class);
                System.out.println("\n📦 응답 본문 (JSON Pretty Print):");
                System.out.println(gson.toJson(jsonResponse));

                // 응답 구조 분석
                System.out.println("\n🔍 응답 데이터 분석:");
                analyzeJsonStructure(jsonResponse, 0);

                // 다운로드 링크 추출
                System.out.println("\n🎯 다운로드 링크 추출:");
                extractDownloadLinks(jsonResponse);

            } catch (Exception e) {
                System.out.println("⚠️  JSON 파싱 실패: " + e.getMessage());
                System.out.println("응답이 JSON 형식이 아닐 수 있습니다.");
            }

            // 검증
            Assertions.assertEquals(200, response.code(), "응답 코드가 200이 아닙니다");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: 전체 플로우 통합 테스트 (msec → convert)")
    void testCompleteFlow() throws IOException, InterruptedException {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔄 통합 테스트: 전체 플로우 실행");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Step 1: msec 조회
        System.out.println("🔹 Step 1: msec 값 조회...");
        Request msecRequest = new Request.Builder()
                .url(BASE_URL + MSEC_ENDPOINT)
                .get()
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .build();

        Double flowMsecValue;
        try (Response msecResponse = client.newCall(msecRequest).execute()) {
            String msecBody = msecResponse.body().string();
            JsonObject msecJson = gson.fromJson(msecBody, JsonObject.class);
            flowMsecValue = msecJson.get("msec").getAsDouble();
            System.out.println("   ✅ msec 값: " + flowMsecValue);
        }

        // 약간의 대기 (실제 브라우저 동작 시뮬레이션)
        Thread.sleep(500);

        // Step 2: convert 요청
        System.out.println("\n🔹 Step 2: convert API 호출...");
        long flowTs = (long) (flowMsecValue * 1000);

        JsonObject flowPayload = new JsonObject();
        flowPayload.addProperty("url", INSTAGRAM_POST_URL);
        flowPayload.addProperty("ts", flowTs);
        flowPayload.addProperty("_s", "54dfb4ab7dc165e718702c5e5772ab930afd7a1cfd127af9b020f563f6d83ba9");
        flowPayload.addProperty("_ts", 1761979938888L);
        flowPayload.addProperty("_tsc", 0);

        RequestBody flowRequestBody = RequestBody.create(
                gson.toJson(flowPayload),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request convertRequest = new Request.Builder()
                .url(BASE_URL + CONVERT_ENDPOINT)
                .post(flowRequestBody)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("content-type", "application/json")
                .addHeader("origin", BASE_URL)
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                .build();

        try (Response convertResponse = client.newCall(convertRequest).execute()) {
            System.out.println("   ✅ 응답 코드: " + convertResponse.code());

            String convertBody = convertResponse.body().string();
            JsonObject convertJson = gson.fromJson(convertBody, JsonObject.class);

            System.out.println("\n📊 최종 결과:");
            System.out.println(gson.toJson(convertJson));

            Assertions.assertEquals(200, convertResponse.code(), "convert API 응답 코드가 200이 아닙니다");
        }

        System.out.println("\n✅ 전체 플로우 테스트 완료!");
    }

    /**
     * JSON 구조를 재귀적으로 분석하여 출력
     */
    private void analyzeJsonStructure(JsonObject json, int depth) {
        String indent = "   ".repeat(depth);

        json.entrySet().forEach(entry -> {
            String key = entry.getKey();
            var value = entry.getValue();

            if (value.isJsonObject()) {
                System.out.println(indent + "🔸 " + key + ": (Object)");
                analyzeJsonStructure(value.getAsJsonObject(), depth + 1);
            } else if (value.isJsonArray()) {
                System.out.println(indent + "🔸 " + key + ": (Array, size=" + value.getAsJsonArray().size() + ")");
                if (value.getAsJsonArray().size() > 0 && value.getAsJsonArray().get(0).isJsonObject()) {
                    System.out.println(indent + "   첫 번째 요소:");
                    analyzeJsonStructure(value.getAsJsonArray().get(0).getAsJsonObject(), depth + 2);
                }
            } else {
                String valueStr = value.toString();
                if (valueStr.length() > 100) {
                    valueStr = valueStr.substring(0, 100) + "...";
                }
                System.out.println(indent + "🔹 " + key + ": " + valueStr);
            }
        });
    }

    /**
     * 응답에서 다운로드 링크 추출
     */
    private void extractDownloadLinks(JsonObject json) {
        int linkCount = 0;

        // 일반적인 필드명들 확인
        String[] possibleFields = {"url", "sd", "thumb", "source", "download_url", "media_url"};

        for (String field : possibleFields) {
            if (json.has(field)) {
                var value = json.get(field);
                if (value.isJsonPrimitive() && !value.isJsonNull()) {
                    String url = value.getAsString();
                    if (url.startsWith("http")) {
                        linkCount++;
                        System.out.println("   " + linkCount + ". [" + field + "] " + url);
                    }
                } else if (value.isJsonObject()) {
                    System.out.println("   🔸 " + field + " (nested object):");
                    extractDownloadLinksFromObject(value.getAsJsonObject(), "      ");
                }
            }
        }

        // 배열 형태의 url 필드 확인
        if (json.has("url") && json.get("url").isJsonArray()) {
            System.out.println("   🔸 url (array):");
            json.getAsJsonArray("url").forEach(item -> {
                if (item.isJsonObject()) {
                    extractDownloadLinksFromObject(item.getAsJsonObject(), "      ");
                }
            });
        }

        if (linkCount == 0) {
            System.out.println("   ⚠️  다운로드 링크를 찾을 수 없습니다.");
        }
    }

    /**
     * JsonObject에서 URL 형태의 값 추출
     */
    private void extractDownloadLinksFromObject(JsonObject obj, String indent) {
        obj.entrySet().forEach(entry -> {
            var value = entry.getValue();
            if (value.isJsonPrimitive() && !value.isJsonNull()) {
                String str = value.getAsString();
                if (str.startsWith("http")) {
                    System.out.println(indent + "- " + entry.getKey() + ": " + str);
                }
            } else if (value.isJsonObject()) {
                System.out.println(indent + "- " + entry.getKey() + ":");
                extractDownloadLinksFromObject(value.getAsJsonObject(), indent + "  ");
            }
        });
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n=================================================");
        System.out.println("🏁 테스트 종료");
        System.out.println("=================================================");

        if (client != null) {
            // OkHttpClient 리소스 정리
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            System.out.println("✅ OkHttpClient 리소스 정리 완료");
        }

        System.out.println("\n📊 쿠키 스토어 최종 상태:");
        if (cookieStore.isEmpty()) {
            System.out.println("   (비어있음)");
        } else {
            cookieStore.forEach((host, cookies) -> {
                System.out.println("   🌐 " + host + ":");
                cookies.forEach(cookie ->
                    System.out.println("      - " + cookie.name() + " = " + cookie.value())
                );
            });
        }
    }
}
