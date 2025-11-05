package com.tripgether.web;

import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 브라우저처럼 완전히 동작 - 실제 브라우저 워크플로우를 OkHttp로 재현
 *
 * 전략:
 * 1. 메인 페이지 로드 → 모든 헤더와 쿠키 저장
 * 2. JavaScript 파일들 다운로드
 * 3. localStorage에 저장될 수 있는 초기화 API 호출 탐색
 * 4. 실제 다운로드 API 호출해서 에러 메시지 분석
 */
public class BrowserMimicTest {

    private static class FullSession {
        private final CookieJar cookieJar;
        private final OkHttpClient client;
        private final Map<String, String> capturedData = new HashMap<>();

        public FullSession() {
            this.cookieJar = new CookieJar() {
                private final Map<String, List<Cookie>> store = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    store.put(url.host(), cookies);
                    System.out.println("🍪 Saved " + cookies.size() + " cookies from " + url.host());
                    for (Cookie cookie : cookies) {
                        System.out.println("   " + cookie.name() + " = " + cookie.value());
                        capturedData.put("cookie_" + cookie.name(), cookie.value());
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = store.get(url.host());
                    return cookies != null ? cookies : new java.util.ArrayList<>();
                }
            };

            this.client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .followRedirects(true)
                    .build();
        }

        public Response get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .addHeader("Accept-Language", "en-US,en;q=0.9")
                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .build();

            Response response = client.newCall(request).execute();
            analyzeResponse(url, response);
            return response;
        }

        private void analyzeResponse(String url, Response response) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("📡 Response from: " + url);
            System.out.println("=".repeat(70));
            System.out.println("Status: " + response.code());

            // Check ALL headers for potential SECRET_KEY or session info
            System.out.println("\n📨 Response Headers:");
            for (String name : response.headers().names()) {
                String value = response.header(name);
                System.out.println("  " + name + ": " + value);

                // Look for suspicious hex values
                if (value != null && value.matches(".*[0-9a-f]{32,}.*")) {
                    System.out.println("    ⭐ Contains hex string!");
                    capturedData.put("header_" + name, value);
                }
            }
        }

        public void printCapturedData() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("📊 All Captured Data:");
            System.out.println("=".repeat(70));
            for (Map.Entry<String, String> entry : capturedData.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    @Test
    void testCompleteBrowserWorkflow() throws Exception {
        FullSession session = new FullSession();

        // Step 1: Load main page (exactly like browser)
        System.out.println("🌐 Step 1: Loading main page...");
        Response mainPage = session.get("https://sssinstagram.com");
        String html = mainPage.body().string();
        System.out.println("HTML size: " + html.length() + " bytes");
        mainPage.close();

        // Step 2: Try to find initialization API that might return SECRET_KEY
        System.out.println("\n🔍 Step 2: Trying common init endpoints...");
        String[] initEndpoints = {
                "https://sssinstagram.com/api/init",
                "https://sssinstagram.com/api/config",
                "https://sssinstagram.com/api/token",
                "https://sssinstagram.com/api/session"
        };

        for (String endpoint : initEndpoints) {
            try {
                Response initResp = session.get(endpoint);
                if (initResp.isSuccessful() && initResp.body() != null) {
                    String body = initResp.body().string();
                    System.out.println("✅ " + endpoint + " returned: " + body);

                    // Check if it contains a 64-char hex
                    if (body.matches(".*[0-9a-f]{64}.*")) {
                        System.out.println("   🎯 CONTAINS 64-CHAR HEX!");
                    }
                }
                initResp.close();
            } catch (Exception e) {
                System.out.println("❌ " + endpoint + " failed: " + e.getMessage());
            }
        }

        // Step 3: Now try the actual convert API with a WRONG signature
        //         The error message might give us hints
        System.out.println("\n🧪 Step 3: Testing convert API with dummy signature...");
        long ts = System.currentTimeMillis();
        long _ts = ts - 10000; // Some session timestamp

        String json = String.format(
                "{\"url\":\"https://www.instagram.com/p/test/\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"dummy\"}",
                ts, _ts
        );

        RequestBody body = RequestBody.create(
                json,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://sssinstagram.com/api/convert")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0")
                .addHeader("Accept", "application/json")
                .addHeader("Origin", "https://sssinstagram.com")
                .addHeader("Referer", "https://sssinstagram.com/")
                .build();

        try (Response response = session.client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";
            System.out.println("Status: " + response.code());
            System.out.println("Response: " + responseBody);

            // The error response might contain hints about the expected format
        }

        // Step 4: Print all captured data
        session.printCapturedData();

        System.out.println("\n💡 Next Step:");
        System.out.println("If no SECRET_KEY found in any response, then it MUST be:");
        System.out.println("1. Hardcoded in JavaScript (need to deobfuscate link.chunk.js)");
        System.out.println("2. Generated from timestamp or other client data");
        System.out.println("3. Computed from multiple sources we haven't identified yet");
    }
}
