package com.tripgether.web;

import okhttp3.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * SECRET_KEY 생성 알고리즘 역공학
 *
 * 가능한 생성 방법들:
 * 1. 서버에서 제공 (API 응답 또는 HTML에 포함)
 * 2. 클라이언트 정보 기반 (User-Agent, 시간 등)
 * 3. 고정 문자열의 해시
 * 4. 무작위 생성 후 localStorage/sessionStorage 저장
 */
public class SecretKeyExtractor {

    private final OkHttpClient client;

    public SecretKeyExtractor() {
        this.client = new OkHttpClient.Builder().build();
    }

    /**
     * 가설 1: 서버에서 SECRET_KEY 제공
     * 메인 페이지 로드 시 응답 헤더나 HTML에 포함
     */
    public String extractFromServerResponse() throws IOException {
        System.out.println("\n🔍 가설 1: 서버 응답에서 SECRET_KEY 찾기");

        Request request = new Request.Builder()
                .url("https://sssinstagram.com")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            // 응답 헤더 확인
            System.out.println("  응답 헤더:");
            response.headers().names().forEach(name -> {
                String value = response.header(name);
                System.out.println("    " + name + ": " + value);
                // X-Secret-Key, X-Session-Key 등 찾기
                if (name.toLowerCase().contains("secret") ||
                    name.toLowerCase().contains("key") ||
                    name.toLowerCase().contains("session")) {
                    System.out.println("    ⭐ 후보 발견!");
                }
            });

            // Set-Cookie 특별 확인
            String cookies = response.header("Set-Cookie");
            if (cookies != null) {
                System.out.println("\n  쿠키 분석:");
                System.out.println("    " + cookies);
            }

            return null;
        }
    }

    /**
     * 가설 2: _ts (세션 타임스탬프) 기반 생성
     */
    public String generateFromTimestamp(long sessionTimestamp) {
        System.out.println("\n🔍 가설 2: 타임스탬프 기반 SECRET_KEY 생성");

        String[] candidates = {
            String.valueOf(sessionTimestamp),
            "sssinstagram" + sessionTimestamp,
            sessionTimestamp + "salt",
            "secret_" + sessionTimestamp,
            sha256(String.valueOf(sessionTimestamp)),
            sha256("sssinstagram" + sessionTimestamp),
        };

        System.out.println("  후보들:");
        for (String candidate : candidates) {
            String hash = sha256(candidate);
            System.out.println("    " + candidate + " → " + hash.substring(0, 20) + "...");
        }

        // 알려진 SECRET_KEY와 비교 필요
        return null;
    }

    /**
     * 가설 3: User-Agent 또는 환경 정보 기반
     */
    public String generateFromEnvironment() {
        System.out.println("\n🔍 가설 3: 환경 정보 기반 SECRET_KEY 생성");

        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";
        String timestamp = String.valueOf(System.currentTimeMillis());

        String[] candidates = {
            sha256(userAgent),
            sha256(userAgent + timestamp),
            sha256("sssinstagram" + userAgent),
        };

        System.out.println("  후보들:");
        for (String candidate : candidates) {
            System.out.println("    " + candidate.substring(0, 20) + "...");
        }

        return null;
    }

    /**
     * 가설 4: JavaScript 초기화 API 호출
     * 페이지 로드 시 별도 API로 SECRET_KEY 요청
     */
    public String fetchFromInitApi() throws IOException {
        System.out.println("\n🔍 가설 4: 초기화 API에서 SECRET_KEY 받기");

        String[] initEndpoints = {
            "https://sssinstagram.com/api/init",
            "https://sssinstagram.com/api/session",
            "https://sssinstagram.com/api/config",
            "https://sssinstagram.com/api/key",
        };

        for (String endpoint : initEndpoints) {
            try {
                Request request = new Request.Builder()
                        .url(endpoint)
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    System.out.println("  ✅ " + endpoint + " 응답:");
                    System.out.println("    " + body.substring(0, Math.min(200, body.length())));

                    // JSON에서 key 추출 시도
                    if (body.contains("secret") || body.contains("key")) {
                        System.out.println("    ⭐ SECRET_KEY 후보 발견!");
                    }
                }
            } catch (IOException e) {
                System.out.println("  ❌ " + endpoint + " 접근 불가");
            }
        }

        return null;
    }

    /**
     * SHA-256 해시 유틸리티
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 모든 가설 테스트
     */
    public void testAllHypotheses() throws IOException {
        System.out.println("=" + "=".repeat(69));
        System.out.println("SECRET_KEY 생성 알고리즘 역공학");
        System.out.println("=" + "=".repeat(69));

        extractFromServerResponse();
        generateFromTimestamp(System.currentTimeMillis());
        generateFromEnvironment();
        fetchFromInitApi();

        System.out.println("\n" + "=" + "=".repeat(69));
        System.out.println("💡 다음 단계:");
        System.out.println("1. Chrome DevTools에서 네트워크 탭 확인");
        System.out.println("2. 페이지 로드 시 모든 요청 분석");
        System.out.println("3. LocalStorage/SessionStorage 확인");
        System.out.println("=" + "=".repeat(69));
    }
}
