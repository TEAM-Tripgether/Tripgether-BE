package com.tripgether.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import okhttp3.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실시간 SHA-256 입력 캡처로 SECRET_KEY 추출
 *
 * 전략:
 * 1. Selenium으로 sssinstagram.com 로드
 * 2. crypto.subtle.digest를 후킹하는 JavaScript 주입
 * 3. Instagram URL 입력 및 다운로드 버튼 클릭
 * 4. 캡처된 SECRET_KEY로 OkHttp API 요청
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RealTimeSecretCaptureTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    private WebDriver driver;
    private WebDriverWait wait;
    private OkHttpClient httpClient;

    @BeforeAll
    void setupClass() {
        System.out.println("=".repeat(70));
        System.out.println("🚀 실시간 SECRET_KEY 캡처 테스트 초기화");
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
        // Headless 모드 OFF - 디버깅을 위해 브라우저 표시
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("실시간 SHA-256 캡처로 SECRET_KEY 추출 → OkHttp 요청")
    void testRealTimeCaptureAndOkHttp() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 실시간 SECRET_KEY 캡처 테스트");
        System.out.println("=".repeat(70));

        // Step 1: 페이지 로드
        System.out.println("\n📍 Step 1: sssinstagram.com 로드...");
        driver.get("https://sssinstagram.com");
        Thread.sleep(2000); // 페이지 로드 대기
        System.out.println("✅ 페이지 로드 완료");

        // Step 2: SHA-256 캡처 스크립트 주입
        System.out.println("\n📍 Step 2: crypto.subtle.digest 후킹 스크립트 주입...");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String captureScript = """
            (function() {
                window.capturedSecret = null;

                if (window.crypto && window.crypto.subtle) {
                    const originalDigest = window.crypto.subtle.digest.bind(window.crypto.subtle);

                    window.crypto.subtle.digest = async function(algorithm, data) {
                        const result = await originalDigest(algorithm, data);

                        if (algorithm === 'SHA-256' || (algorithm && algorithm.name === 'SHA-256')) {
                            let inputString = '';
                            if (data instanceof ArrayBuffer) {
                                inputString = new TextDecoder().decode(data);
                            } else {
                                inputString = new TextDecoder().decode(new Uint8Array(data));
                            }

                            // Instagram URL이 포함된 경우
                            if (inputString.includes('instagram.com')) {
                                console.log('🔐 SHA-256 입력 캡처!');
                                console.log('전체 입력:', inputString);
                                console.log('길이:', inputString.length);

                                // URL 파싱
                                const url = inputString.match(/https:\\/\\/www\\.instagram\\.com[^\\s]+/);
                                if (url && url[0]) {
                                    const afterUrl = inputString.substring(url[0].length);
                                    const ts = afterUrl.substring(0, 13);
                                    const secret = afterUrl.substring(13);

                                    console.log('Timestamp:', ts);
                                    console.log('Secret:', secret);
                                    console.log('Secret 길이:', secret.length);

                                    // 전역 변수에 저장
                                    window.capturedSecret = secret;
                                    window.capturedTimestamp = ts;
                                    window.capturedInput = inputString;
                                }
                            }
                        }

                        return result;
                    };

                    console.log('✅ SHA-256 캡처 준비 완료!');
                    return true;
                } else {
                    console.error('❌ crypto.subtle API를 사용할 수 없습니다');
                    return false;
                }
            })();
            """;

        Object result = js.executeScript(captureScript);
        System.out.println("  스크립트 실행 결과: " + result);
        System.out.println("✅ 캡처 스크립트 주입 완료");

        // Step 3: Instagram URL 입력
        System.out.println("\n📍 Step 3: Instagram URL 입력...");
        WebElement urlInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='text'], input[placeholder*='URL'], input[placeholder*='url']")
        ));
        urlInput.clear();
        urlInput.sendKeys(TEST_URL);
        System.out.println("✅ URL 입력 완료");

        // Step 4: 다운로드 버튼 클릭
        System.out.println("\n📍 Step 4: 다운로드 버튼 클릭...");
        WebElement downloadButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit'], button")
        ));
        downloadButton.click();
        System.out.println("✅ 다운로드 버튼 클릭 완료");

        // Step 5: SECRET_KEY 캡처 대기
        System.out.println("\n📍 Step 5: SECRET_KEY 캡처 대기...");
        Thread.sleep(3000); // API 호출 대기

        // 캡처된 SECRET_KEY 확인
        String capturedSecret = (String) js.executeScript("return window.capturedSecret;");
        String capturedTimestamp = (String) js.executeScript("return window.capturedTimestamp;");
        String capturedInput = (String) js.executeScript("return window.capturedInput;");

        System.out.println("\n📥 캡처 결과:");
        System.out.println("  Captured Secret: " + capturedSecret);
        System.out.println("  Captured Timestamp: " + capturedTimestamp);
        if (capturedInput != null) {
            System.out.println("  Captured Input Length: " + capturedInput.length());
        }

        assertNotNull(capturedSecret, "SECRET_KEY 캡처 실패");
        assertEquals(64, capturedSecret.length(), "SECRET_KEY는 64자여야 합니다");
        System.out.println("✅ SECRET_KEY 캡처 성공!");

        // Step 6: OkHttp로 API 요청
        System.out.println("\n📍 Step 6: 캡처한 SECRET_KEY로 OkHttp API 요청...");
        long ts = System.currentTimeMillis();
        String signature = sha256(TEST_URL + ts + capturedSecret);

        String requestJson = String.format(
            "{\"url\":\"%s\",\"ts\":%d,\"_ts\":%d,\"_tsc\":0,\"_s\":\"%s\"}",
            TEST_URL, ts, ts, signature
        );

        System.out.println("  OkHttp Timestamp: " + ts);
        System.out.println("  OkHttp Signature: " + signature.substring(0, 32) + "...");

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

            System.out.println("\n📥 OkHttp API 응답:");
            System.out.println("  상태 코드: " + response.code());
            System.out.println("  응답 본문: " + responseBody);

            System.out.println("\n" + "=".repeat(70));

            if (response.code() == 401) {
                System.err.println("❌ 401 에러 - SECRET_KEY는 캡처했지만 여전히 실패");
                System.err.println("⚠️  가능한 원인:");
                System.err.println("   1. SECRET_KEY가 세션/쿠키와 연결됨");
                System.err.println("   2. 타임스탬프 유효 시간 제한");
                System.err.println("   3. IP 주소나 User-Agent 검증");
                System.err.println("💡 해결책: Selenium에서 직접 API 호출 필요");
                fail("API returned 401: " + responseBody);
            } else if (response.isSuccessful()) {
                System.out.println("✅ 성공! Selenium 캡처 + OkHttp 조합 작동!");
                assertTrue(true);
            } else {
                fail("Unexpected response code: " + response.code());
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
