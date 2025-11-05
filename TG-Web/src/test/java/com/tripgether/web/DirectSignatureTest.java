package com.tripgether.web;

import okhttp3.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 콘솔 캡처 데이터로 정확한 서명 재현
 *
 * 콘솔 로그:
 * URL: https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp
 * Timestamp: 1762280315477
 * Secret: 99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee
 *
 * SHA-256 입력: URL + Timestamp + Secret
 */
class DirectSignatureTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
    private static final String SECRET_KEY = "99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Test
    @DisplayName("콘솔 캡처 데이터로 정확한 서명 재현")
    void testExactSignatureFromConsole() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 콘솔 로그 재현 테스트");
        System.out.println("=".repeat(70));

        // 콘솔에서 캡처한 타임스탬프 사용
        long capturedTimestamp = 1762280315477L;

        // 콘솔 로그와 동일한 입력 생성
        String input = TEST_URL + capturedTimestamp + SECRET_KEY;

        System.out.println("📝 입력 데이터:");
        System.out.println("  URL: " + TEST_URL);
        System.out.println("  Timestamp: " + capturedTimestamp);
        System.out.println("  Secret: " + SECRET_KEY);
        System.out.println("\n🔗 연결된 입력:");
        System.out.println("  " + input);
        System.out.println("  길이: " + input.length());

        // SHA-256 해시 생성
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String signature = hexString.toString();

        System.out.println("\n✍️ 생성된 서명:");
        System.out.println("  " + signature);

        // API 호출
        String requestJson = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            TEST_URL, capturedTimestamp, capturedTimestamp, signature
        );

        System.out.println("\n📤 API 요청:");
        System.out.println("  " + requestJson);

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

        System.out.println("\n🌐 API 호출 중...");

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";

            System.out.println("\n📥 응답:");
            System.out.println("  상태 코드: " + response.code());
            System.out.println("  응답 본문: " + responseBody);

            System.out.println("\n" + "=".repeat(70));

            if (response.code() == 401) {
                System.err.println("❌ 401 에러 - 서명이 여전히 일치하지 않습니다");
                System.err.println("⚠️  가능한 원인:");
                System.err.println("   1. SECRET_KEY가 시간에 따라 변경됨 (동적 생성)");
                System.err.println("   2. 타임스탬프가 너무 오래됨 (시간 제한)");
                System.err.println("   3. 추가 헤더나 쿠키 필요");
                fail("API returned 401: " + responseBody);
            }

            assertTrue(response.isSuccessful(), "API should return success");
            assertNotNull(responseBody);
            assertFalse(responseBody.isEmpty());

            System.out.println("✅ 성공!");
        }
    }

    @Test
    @DisplayName("현재 타임스탬프로 API 호출 (SECRET_KEY 만료 여부 확인)")
    void testWithCurrentTimestamp() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⏰ 현재 타임스탬프 테스트");
        System.out.println("=".repeat(70));

        long currentTimestamp = System.currentTimeMillis();

        String input = TEST_URL + currentTimestamp + SECRET_KEY;
        String signature = sha256(input);

        System.out.println("📝 입력 데이터:");
        System.out.println("  Timestamp: " + currentTimestamp + " (현재 시각)");
        System.out.println("  서명: " + signature);

        String requestJson = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            TEST_URL, currentTimestamp, currentTimestamp, signature
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

        System.out.println("🌐 API 호출 중...");

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";

            System.out.println("\n📥 응답:");
            System.out.println("  상태 코드: " + response.code());
            System.out.println("  응답 본문: " + responseBody);

            System.out.println("\n" + "=".repeat(70));

            if (response.code() == 401) {
                System.err.println("❌ 401 에러 - SECRET_KEY가 시간에 따라 변경되거나 만료되었습니다");
                System.err.println("💡 해결책: Selenium으로 실시간 SECRET_KEY 추출 필요");
            } else {
                System.out.println("✅ 성공! SECRET_KEY는 고정값입니다.");
            }
        }
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
