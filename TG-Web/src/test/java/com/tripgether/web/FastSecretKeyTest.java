package com.tripgether.web;

import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 빠른 SECRET_KEY 추출 테스트
 *
 * 목표: Selenium 없이 OkHttp만으로 SECRET_KEY 찾기
 */
public class FastSecretKeyTest {

    @Test
    void testExtractSecretKeyFromMainPage() throws IOException {
        System.out.println("=" + "=".repeat(69));
        System.out.println("🔍 메인 페이지에서 SECRET_KEY 추출 시도");
        System.out.println("=" + "=".repeat(69));

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();

        Request request = new Request.Builder()
                .url("https://sssinstagram.com")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("\n📊 응답 정보:");
            System.out.println("  상태 코드: " + response.code());
            System.out.println("  URL: " + response.request().url());

            String html = response.body() != null ? response.body().string() : "";
            System.out.println("  HTML 크기: " + html.length() + " bytes");

            // 패턴 1: 64자 hex 문자열 찾기
            System.out.println("\n🔍 패턴 검색:");
            Pattern hexPattern = Pattern.compile("([0-9a-f]{64})");
            Matcher hexMatcher = hexPattern.matcher(html);

            int count = 0;
            while (hexMatcher.find() && count < 5) {
                String hex = hexMatcher.group(1);
                System.out.println("  후보 " + (++count) + ": " + hex.substring(0, 20) + "...");

                // 알려진 SECRET_KEY와 비교
                if (hex.equals("99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee")) {
                    System.out.println("    ✅ 알려진 SECRET_KEY 발견!");
                }
            }

            // 패턴 2: JavaScript 파일 URL 찾기
            System.out.println("\n📜 JavaScript 파일들:");
            Pattern scriptPattern = Pattern.compile("<script[^>]+src=[\"']([^\"']+)[\"']");
            Matcher scriptMatcher = scriptPattern.matcher(html);

            while (scriptMatcher.find()) {
                String scriptUrl = scriptMatcher.group(1);
                System.out.println("  - " + scriptUrl);
            }

            // 패턴 3: localStorage/sessionStorage 초기화 코드
            if (html.contains("localStorage") || html.contains("sessionStorage")) {
                System.out.println("\n💾 Storage 사용 감지!");
                System.out.println("  → SECRET_KEY가 브라우저 Storage에 저장될 가능성");
            }

            // 패턴 4: API 엔드포인트 찾기
            Pattern apiPattern = Pattern.compile("/api/([a-zA-Z]+)");
            Matcher apiMatcher = apiPattern.matcher(html);
            System.out.println("\n🌐 발견된 API 엔드포인트:");
            while (apiMatcher.find()) {
                System.out.println("  - /api/" + apiMatcher.group(1));
            }
        }
    }

    @Test
    void testFetchJavaScriptFiles() throws IOException {
        System.out.println("\n" + "=" + "=".repeat(69));
        System.out.println("📥 JavaScript 파일 다운로드 및 분석");
        System.out.println("=" + "=".repeat(69));

        OkHttpClient client = new OkHttpClient();

        // 알려진 JavaScript 파일들
        String[] jsUrls = {
            "https://sssinstagram.com/assets/app.js",
            "https://sssinstagram.com/assets/link.chunk.js",
            "https://sssinstagram.com/js/app.js",
            "https://sssinstagram.com/static/js/main.js"
        };

        for (String jsUrl : jsUrls) {
            try {
                Request request = new Request.Builder()
                        .url(jsUrl)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsContent = response.body().string();
                    System.out.println("\n✅ " + jsUrl);
                    System.out.println("   크기: " + jsContent.length() + " bytes");

                    // SECRET_KEY 패턴 검색
                    Pattern hexPattern = Pattern.compile("([0-9a-f]{64})");
                    Matcher hexMatcher = hexPattern.matcher(jsContent);

                    int count = 0;
                    while (hexMatcher.find() && count < 3) {
                        String hex = hexMatcher.group(1);
                        System.out.println("   후보 " + (++count) + ": " + hex.substring(0, 20) + "...");
                    }

                    // 특정 패턴들
                    if (jsContent.contains("99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee")) {
                        System.out.println("   🎯 알려진 SECRET_KEY 하드코딩되어 있음!");
                    }

                    if (jsContent.contains("localStorage.setItem")) {
                        System.out.println("   💾 localStorage 사용 코드 발견");
                    }
                }
            } catch (IOException e) {
                System.out.println("\n❌ " + jsUrl + " 접근 실패");
            }
        }
    }

    @Test
    void testCheckInitializationEndpoints() throws IOException {
        System.out.println("\n" + "=" + "=".repeat(69));
        System.out.println("🔧 초기화 API 엔드포인트 탐색");
        System.out.println("=" + "=".repeat(69));

        OkHttpClient client = new OkHttpClient();

        String[] endpoints = {
            "https://sssinstagram.com/api/init",
            "https://sssinstagram.com/api/config",
            "https://sssinstagram.com/api/session",
            "https://sssinstagram.com/api/key",
            "https://sssinstagram.com/api/secret"
        };

        for (String endpoint : endpoints) {
            try {
                Request request = new Request.Builder()
                        .url(endpoint)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .addHeader("Accept", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                System.out.println("\n" + endpoint);
                System.out.println("  상태: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    System.out.println("  응답: " + body.substring(0, Math.min(200, body.length())));

                    // SECRET_KEY 패턴 찾기
                    if (body.contains("99ee6bfc") || body.matches(".*[0-9a-f]{64}.*")) {
                        System.out.println("  🎯 SECRET_KEY 후보 발견!");
                    }
                }
            } catch (IOException e) {
                System.out.println("\n" + endpoint + " ❌ 접근 불가");
            }
        }
    }

    @Test
    void testServerProvidedSecret() throws IOException {
        System.out.println("\n" + "=" + "=".repeat(69));
        System.out.println("🔐 서버 제공 SECRET_KEY 확인");
        System.out.println("=" + "=".repeat(69));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://sssinstagram.com")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("\n📨 응답 헤더 분석:");

            // 모든 헤더 확인
            response.headers().names().forEach(name -> {
                String value = response.header(name);
                System.out.println("  " + name + ": " + value);

                // 의심스러운 헤더 강조
                String lowerName = name.toLowerCase();
                if (lowerName.contains("secret") ||
                    lowerName.contains("key") ||
                    lowerName.contains("session") ||
                    lowerName.contains("token")) {
                    System.out.println("    ⭐ 주목!");
                }
            });

            // 쿠키 확인
            System.out.println("\n🍪 쿠키 분석:");
            String setCookie = response.header("Set-Cookie");
            if (setCookie != null) {
                System.out.println("  " + setCookie);
            } else {
                System.out.println("  쿠키 없음");
            }
        }
    }
}
