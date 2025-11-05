package com.tripgether.web;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CookieJar를 사용한 세션 유지 테스트
 *
 * 전략:
 * 1. CookieJar로 세션 쿠키 자동 저장/전송
 * 2. 먼저 sssinstagram.com 메인 페이지 방문하여 쿠키 획득
 * 3. 획득한 쿠키로 API 요청 시도
 */
class CookieJarInstagramTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
    private static final String SECRET_KEY = "99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee";

    private OkHttpClient client;
    private Map<String, List<Cookie>> cookieStore;

    @BeforeEach
    void setUp() {
        cookieStore = new HashMap<>();

        // CookieJar 구현
        CookieJar cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                String host = url.host();
                cookieStore.put(host, cookies);
                System.out.println("🍪 저장된 쿠키 (" + host + "):");
                for (Cookie cookie : cookies) {
                    System.out.println("  " + cookie.name() + " = " + cookie.value());
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                String host = url.host();
                List<Cookie> cookies = cookieStore.getOrDefault(host, new ArrayList<>());
                if (!cookies.isEmpty()) {
                    System.out.println("🍪 전송할 쿠키 (" + host + "):");
                    for (Cookie cookie : cookies) {
                        System.out.println("  " + cookie.name() + " = " + cookie.value());
                    }
                }
                return cookies;
            }
        };

        // OkHttpClient with CookieJar
        this.client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    @Test
    @DisplayName("Step 1: 메인 페이지 방문 → 쿠키 획득")
    void testStep1_GetCookies() throws IOException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🌐 Step 1: sssinstagram.com 메인 페이지 방문");
        System.out.println("=".repeat(70));

        Request request = new Request.Builder()
                .url("https://sssinstagram.com")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("📥 응답 상태: " + response.code());
            System.out.println("📄 콘텐츠 타입: " + response.header("Content-Type"));

            assertTrue(response.isSuccessful(), "메인 페이지 로드 실패");
            assertFalse(cookieStore.isEmpty(), "쿠키가 저장되지 않았습니다");

            System.out.println("✅ 쿠키 획득 완료");
            System.out.println("=".repeat(70));
        }
    }

    @Test
    @DisplayName("Step 2: 쿠키와 함께 API 요청")
    void testStep2_ApiWithCookies() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 Step 2: 쿠키를 사용한 API 요청");
        System.out.println("=".repeat(70));

        // Step 1: 메인 페이지 방문하여 쿠키 획득
        System.out.println("\n📍 Phase 1: 메인 페이지 방문...");
        Request homeRequest = new Request.Builder()
                .url("https://sssinstagram.com")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build();

        try (Response homeResponse = client.newCall(homeRequest).execute()) {
            System.out.println("✅ 메인 페이지 로드 완료: " + homeResponse.code());
        }

        // 잠시 대기 (브라우저 동작 모방)
        Thread.sleep(500);

        // Step 2: API 요청
        System.out.println("\n📍 Phase 2: API 요청...");
        long ts = System.currentTimeMillis();
        String signature = sha256(TEST_URL + ts + SECRET_KEY);

        String requestJson = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            TEST_URL, ts, ts, signature
        );

        System.out.println("📤 요청 데이터:");
        System.out.println("  Timestamp: " + ts);
        System.out.println("  Signature: " + signature.substring(0, 32) + "...");

        RequestBody body = RequestBody.create(
            requestJson,
            MediaType.get("application/json; charset=utf-8")
        );

        Request apiRequest = new Request.Builder()
                .url("https://sssinstagram.com/api/convert")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Origin", "https://sssinstagram.com")
                .addHeader("Referer", "https://sssinstagram.com/")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .build();

        try (Response apiResponse = client.newCall(apiRequest).execute()) {
            String responseBody = apiResponse.body() != null ? apiResponse.body().string() : "null";

            System.out.println("\n📥 API 응답:");
            System.out.println("  상태 코드: " + apiResponse.code());
            System.out.println("  응답 본문: " + responseBody);

            System.out.println("\n" + "=".repeat(70));

            if (apiResponse.code() == 401) {
                System.err.println("❌ 여전히 401 에러 발생");
                System.err.println("⚠️  쿠키만으로는 해결되지 않음");
                System.err.println("💡 다음 단계: SECRET_KEY 동적 추출 필요");
                fail("API returned 401 even with cookies: " + responseBody);
            } else {
                System.out.println("✅ 성공! 쿠키가 문제를 해결했습니다.");
                assertTrue(apiResponse.isSuccessful());
            }
        }
    }

    @Test
    @DisplayName("Complete Flow: 쿠키 획득 → 여러 번 API 호출")
    void testCompleteFlow() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔄 Complete Flow 테스트");
        System.out.println("=".repeat(70));

        // Step 1: 메인 페이지 방문
        System.out.println("\n📍 메인 페이지 방문...");
        Request homeRequest = new Request.Builder()
                .url("https://sssinstagram.com")
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .build();

        try (Response homeResponse = client.newCall(homeRequest).execute()) {
            System.out.println("✅ 메인 페이지: " + homeResponse.code());
        }

        Thread.sleep(500);

        // Step 2: 여러 번 API 호출 (세션 유지 테스트)
        System.out.println("\n📍 API 호출 3회 반복...");
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n🔄 시도 " + i + "/3:");

            long ts = System.currentTimeMillis();
            String signature = sha256(TEST_URL + ts + SECRET_KEY);

            String requestJson = String.format(
                "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
                TEST_URL, ts, ts, signature
            );

            RequestBody body = RequestBody.create(
                requestJson,
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://sssinstagram.com/api/convert")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .addHeader("Accept", "application/json")
                    .addHeader("Origin", "https://sssinstagram.com")
                    .addHeader("Referer", "https://sssinstagram.com/")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "null";

                System.out.println("  상태: " + response.code());

                if (response.code() == 401) {
                    System.out.println("  ❌ 401 에러");
                } else if (response.isSuccessful()) {
                    System.out.println("  ✅ 성공: " + responseBody.substring(0, Math.min(100, responseBody.length())));
                }
            }

            Thread.sleep(1000); // 요청 간 간격
        }

        System.out.println("\n" + "=".repeat(70));
    }

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
