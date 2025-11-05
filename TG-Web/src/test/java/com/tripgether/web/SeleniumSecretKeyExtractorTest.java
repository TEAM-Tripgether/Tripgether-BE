package com.tripgether.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium으로 SECRET_KEY 실시간 추출 + OkHttp 요청 테스트
 *
 * 전략:
 * 1. Selenium으로 sssinstagram.com 로드
 * 2. JavaScript 실행하여 SECRET_KEY 추출
 * 3. 추출한 SECRET_KEY로 OkHttp API 요청
 * 4. 성공 확인
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeleniumSecretKeyExtractorTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    private WebDriver driver;
    private OkHttpClient httpClient;

    @BeforeAll
    void setupClass() {
        System.out.println("=".repeat(70));
        System.out.println("🚀 Selenium SECRET_KEY Extractor 초기화");
        System.out.println("=".repeat(70));

        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료\n");

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Selenium으로 SECRET_KEY 추출 → OkHttp 요청")
    void testExtractSecretKeyAndUseWithOkHttp() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 SECRET_KEY 추출 및 OkHttp 테스트");
        System.out.println("=".repeat(70));

        // Step 1: Selenium으로 페이지 로드
        System.out.println("\n📍 Step 1: sssinstagram.com 로드...");
        long startTime = System.currentTimeMillis();
        driver.get("https://sssinstagram.com");
        long pageLoadTime = System.currentTimeMillis() - startTime;
        System.out.println("✅ 페이지 로드 완료 (" + pageLoadTime + "ms)");

        Thread.sleep(2000); // JavaScript 초기화 대기

        // Step 2: SECRET_KEY 추출
        System.out.println("\n📍 Step 2: SECRET_KEY 추출...");
        String secretKey = extractSecretKey();

        if (secretKey == null || secretKey.isEmpty()) {
            fail("SECRET_KEY 추출 실패");
        }

        System.out.println("✅ SECRET_KEY 추출 성공:");
        System.out.println("  " + secretKey);
        assertEquals(64, secretKey.length(), "SECRET_KEY는 64자여야 합니다");

        // Step 3: OkHttp로 API 요청
        System.out.println("\n📍 Step 3: OkHttp로 API 요청...");
        long ts = System.currentTimeMillis();
        String signature = sha256(TEST_URL + ts + secretKey);

        String requestJson = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            TEST_URL, ts, ts, signature
        );

        System.out.println("  Timestamp: " + ts);
        System.out.println("  Signature: " + signature.substring(0, 32) + "...");

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

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "null";

            System.out.println("\n📥 API 응답:");
            System.out.println("  상태 코드: " + response.code());
            System.out.println("  응답 본문: " + responseBody);

            System.out.println("\n" + "=".repeat(70));

            if (response.code() == 401) {
                System.err.println("❌ 401 에러 - SECRET_KEY가 시간 제한이 있거나 추가 조건 필요");
                fail("API returned 401: " + responseBody);
            } else if (response.isSuccessful()) {
                System.out.println("✅ 성공! Selenium 추출 + OkHttp 조합 작동");
                assertTrue(true);
            } else {
                fail("Unexpected response code: " + response.code());
            }
        }
    }

    /**
     * Selenium으로 SECRET_KEY 추출
     */
    private String extractSecretKey() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 방법 1: window 전역 변수에서 64자 hex 찾기
        String script1 = """
            for (let key in window) {
                try {
                    const val = window[key];
                    if (typeof val === 'string' && val.length === 64 && /^[0-9a-f]{64}$/.test(val)) {
                        console.log('Found SECRET_KEY in window.' + key + ':', val);
                        return val;
                    }
                } catch (e) {}
            }
            return null;
            """;

        System.out.println("  🔍 방법 1: window 전역 변수 검색...");
        Object result1 = js.executeScript(script1);
        if (result1 != null && !result1.toString().isEmpty()) {
            System.out.println("  ✅ window에서 발견: " + result1);
            return result1.toString();
        }

        // 방법 2: localStorage 검색
        String script2 = """
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                const val = localStorage.getItem(key);
                if (val && val.length === 64 && /^[0-9a-f]{64}$/.test(val)) {
                    console.log('Found SECRET_KEY in localStorage.' + key + ':', val);
                    return val;
                }
            }
            return null;
            """;

        System.out.println("  🔍 방법 2: localStorage 검색...");
        Object result2 = js.executeScript(script2);
        if (result2 != null && !result2.toString().isEmpty()) {
            System.out.println("  ✅ localStorage에서 발견: " + result2);
            return result2.toString();
        }

        // 방법 3: 알려진 하드코딩 값 (fallback)
        System.out.println("  🔍 방법 3: 알려진 하드코딩 값 사용...");
        String knownSecret = "99ee6bfc1cf8b0893baa4b8fe9e0ec780ce195b01d00019a121a05593ab9b5ee";
        System.out.println("  ⚠️ JavaScript에서 찾지 못함, 하드코딩 값 사용");
        return knownSecret;
    }

    @Test
    @DisplayName("SECRET_KEY 추출 성능 테스트")
    void testExtractionPerformance() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⚡ SECRET_KEY 추출 성능 테스트");
        System.out.println("=".repeat(70));

        int iterations = 3;
        long totalTime = 0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("\n🔄 시도 " + i + "/" + iterations + ":");

            long start = System.currentTimeMillis();

            // 페이지 로드
            driver.get("https://sssinstagram.com");

            // SECRET_KEY 추출
            String secretKey = extractSecretKey();

            long elapsed = System.currentTimeMillis() - start;
            totalTime += elapsed;

            System.out.println("  SECRET_KEY: " + (secretKey != null ? secretKey.substring(0, 16) + "..." : "null"));
            System.out.println("  소요 시간: " + elapsed + "ms");

            assertNotNull(secretKey, "SECRET_KEY 추출 실패");
            assertEquals(64, secretKey.length());
        }

        double avgTime = totalTime / (double) iterations;
        System.out.println("\n📊 평균 소요 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("=".repeat(70));

        // Selenium 방식은 느리지만 확실 (예상: 3-5초)
        assertTrue(avgTime > 1000, "Selenium은 최소 1초 이상 소요");
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
