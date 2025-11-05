package com.tripgether.web;

import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SignatureGenerator 테스트
 *
 * Chrome DevTools로 추출한 실제 알고리즘을 검증합니다.
 * 알고리즘: SHA-256(url + timestamp + secret_key)
 */
public class SignatureGeneratorTest {

    private SignatureGenerator signatureGenerator;
    private OkHttpClient client;

    @BeforeEach
    void setUp() {
        signatureGenerator = new SignatureGenerator();
        client = new OkHttpClient();
    }

    @AfterEach
    void tearDown() {
        signatureGenerator.close();
    }

    /**
     * Test 1: 서명 생성 기본 테스트
     */
    @Test
    void testBasicSignatureGeneration() {
        // Given: Instagram URL
        String url = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
        long ts = System.currentTimeMillis();

        // When: 서명 생성
        String signature = signatureGenerator.generateSignature(url, ts);

        // Then: 64자 hex 문자열이어야 함
        assertNotNull(signature);
        assertEquals(64, signature.length());
        assertTrue(signature.matches("[0-9a-f]{64}"));

        System.out.println("Generated signature: " + signature);
        System.out.println("URL: " + url);
        System.out.println("Timestamp: " + ts);
    }

    /**
     * Test 2: 서명 일관성 테스트
     * 동일한 입력에 대해 동일한 서명이 생성되어야 함
     */
    @Test
    void testSignatureConsistency() {
        // Given: 동일한 입력
        String url = "https://www.instagram.com/p/test/";
        long ts = 1234567890123L;

        // When: 같은 입력으로 두 번 서명 생성
        String sig1 = signatureGenerator.generateSignature(url, ts);
        String sig2 = signatureGenerator.generateSignature(url, ts);

        // Then: 동일한 서명이어야 함
        assertEquals(sig1, sig2);
        System.out.println("Signature consistency verified: " + sig1);
    }

    /**
     * Test 3: 다른 URL에 대한 다른 서명
     */
    @Test
    void testDifferentUrls() {
        // Given: 다른 URL들
        String url1 = "https://www.instagram.com/p/test1/";
        String url2 = "https://www.instagram.com/p/test2/";
        long ts = System.currentTimeMillis();

        // When: 서명 생성
        String sig1 = signatureGenerator.generateSignature(url1, ts);
        String sig2 = signatureGenerator.generateSignature(url2, ts);

        // Then: 서명이 달라야 함
        assertNotEquals(sig1, sig2);
        System.out.println("URL1 signature: " + sig1);
        System.out.println("URL2 signature: " + sig2);
    }

    /**
     * Test 4: 다른 타임스탬프에 대한 다른 서명
     */
    @Test
    void testDifferentTimestamps() {
        // Given: 동일 URL, 다른 타임스탬프
        String url = "https://www.instagram.com/p/test/";
        long ts1 = 1000000000000L;
        long ts2 = 2000000000000L;

        // When: 서명 생성
        String sig1 = signatureGenerator.generateSignature(url, ts1);
        String sig2 = signatureGenerator.generateSignature(url, ts2);

        // Then: 서명이 달라야 함
        assertNotEquals(sig1, sig2);
        System.out.println("TS1 signature: " + sig1);
        System.out.println("TS2 signature: " + sig2);
    }

    /**
     * Test 5: 전체 요청 데이터 생성 테스트
     */
    @Test
    void testCreateSignedRequest() {
        // Given: Instagram URL
        String url = "https://www.instagram.com/p/test/";

        // When: 서명된 요청 데이터 생성
        SignatureGenerator.SignatureData data = signatureGenerator.createSignedRequest(url);

        // Then: 모든 필드가 올바르게 설정되어야 함
        assertNotNull(data);
        assertEquals(url, data.url);
        assertTrue(data.ts > 0);
        assertTrue(data._ts > 0);
        assertEquals(0, data._tsc);
        assertNotNull(data._s);
        assertEquals(64, data._s.length());

        System.out.println("Request data: " + data);
    }

    /**
     * Test 6: 실제 API 호출 테스트 (통합 테스트)
     */
    @Test
    void testRealApiCall() throws IOException {
        // Given: 실제 Instagram URL
        String url = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";
        SignatureGenerator.SignatureData data = signatureGenerator.createSignedRequest(url);

        // When: API 호출
        String json = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":%d,\"_s\":\"%s\"}",
            data.url, data.ts, data._ts, data._tsc, data._s
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
            .build();

        System.out.println("Request JSON: " + json);

        try (Response response = client.newCall(request).execute()) {
            // Then: 응답 확인
            System.out.println("Status: " + response.code());
            String responseBody = response.body() != null ? response.body().string() : "null";
            System.out.println("Response: " + responseBody);

            // 200 OK 또는 다른 성공 응답이어야 함
            // 401 Unauthorized가 아니면 서명이 올바른 것
            assertNotEquals(401, response.code(), "Signature should be valid (not 401 Unauthorized)");
        }
    }
}
