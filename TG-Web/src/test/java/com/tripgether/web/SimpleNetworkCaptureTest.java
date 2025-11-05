package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance 로그로 네트워크 응답 캡처 (가장 간단한 방법)
 *
 * 전략:
 * 1. Performance 로그 활성화
 * 2. 다운로드 버튼 클릭
 * 3. 로그에서 /api/convert 응답 찾기
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleNetworkCaptureTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    private ChromeDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        System.out.println("=".repeat(70));
        System.out.println("🚀 Performance 로그 캡처 테스트 초기화");
        System.out.println("=".repeat(70));

        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료\n");
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();

        // Performance 로그 활성화
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        // Headless 모드 (빠른 실행)
        options.addArguments("--headless");
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
    @DisplayName("Performance 로그로 /api/convert 응답 캡처")
    void testCaptureWithPerformanceLog() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 Performance 로그 캡처 테스트");
        System.out.println("=".repeat(70));

        // Step 1: 페이지 로드
        System.out.println("\n📍 Step 1: sssinstagram.com 로드...");
        driver.get("https://sssinstagram.com");
        Thread.sleep(2000);
        System.out.println("✅ 페이지 로드 완료");

        // Step 2: Instagram URL 입력
        System.out.println("\n📍 Step 2: Instagram URL 입력...");
        WebElement urlInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='text'], input")
        ));
        urlInput.clear();
        urlInput.sendKeys(TEST_URL);
        System.out.println("✅ URL 입력 완료");

        // Step 3: 다운로드 버튼 클릭
        System.out.println("\n📍 Step 3: 다운로드 버튼 클릭...");
        WebElement downloadButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button")
        ));
        downloadButton.click();
        System.out.println("✅ 다운로드 버튼 클릭 완료");

        // Step 4: API 응답 대기
        System.out.println("\n📍 Step 4: API 응답 대기...");
        Thread.sleep(5000); // 충분한 시간 대기

        // Step 5: Performance 로그에서 응답 추출
        System.out.println("\n📍 Step 5: Performance 로그 분석...");
        LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);

        String apiResponse = null;
        int logCount = 0;

        for (LogEntry entry : logs) {
            logCount++;
            String logMessage = entry.getMessage();

            try {
                JsonObject logJson = JsonParser.parseString(logMessage).getAsJsonObject();
                JsonObject message = logJson.getAsJsonObject("message");
                String method = message.get("method").getAsString();
                JsonObject params = message.getAsJsonObject("params");

                // Network.responseReceived 이벤트 찾기
                if ("Network.responseReceived".equals(method)) {
                    JsonObject response = params.getAsJsonObject("response");
                    String url = response.get("url").getAsString();
                    int statusCode = response.get("status").getAsInt();

                    if (url.contains("/api/convert")) {
                        System.out.println("\n🎯 API 요청 발견!");
                        System.out.println("  URL: " + url);
                        System.out.println("  상태 코드: " + statusCode);

                        if (statusCode == 200) {
                            // RequestId로 응답 본문 가져오기 시도
                            String requestId = params.get("requestId").getAsString();
                            System.out.println("  Request ID: " + requestId);

                            // Network.getResponseBody를 위해 추가 로그 확인
                            // 일반적으로 응답 본문은 별도 이벤트에 있음
                        }
                    }
                }

                // Network.loadingFinished 또는 다른 이벤트에서 응답 찾기
                if ("Network.loadingFinished".equals(method)) {
                    String requestId = params.get("requestId").getAsString();
                    // 여기서 응답 본문 매칭 시도
                }

            } catch (Exception e) {
                // JSON 파싱 실패 무시
            }
        }

        System.out.println("\n📊 총 로그 엔트리: " + logCount + "개");

        if (apiResponse == null) {
            System.err.println("⚠️  Performance 로그에서 응답 본문을 직접 가져올 수 없습니다");
            System.err.println("💡 대안: 페이지의 DOM에서 다운로드 링크 직접 추출");

            // Step 6: DOM에서 다운로드 링크 추출
            System.out.println("\n📍 Step 6: DOM에서 다운로드 링크 추출...");
            Thread.sleep(2000);

            // 다운로드 링크 찾기
            try {
                WebElement downloadLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("a[download], a[href*='instagram'], a[href*='cdninstagram']")
                ));

                String downloadUrl = downloadLink.getAttribute("href");
                System.out.println("\n🎉 다운로드 URL 추출 성공!");
                System.out.println("  " + downloadUrl);

                assertNotNull(downloadUrl);
                assertTrue(downloadUrl.startsWith("http"), "유효한 URL이어야 합니다");

                System.out.println("\n✅ DOM에서 직접 추출 성공!");
            } catch (Exception e) {
                System.err.println("❌ DOM에서도 링크를 찾지 못했습니다: " + e.getMessage());
                fail("다운로드 링크 추출 실패");
            }
        }

        System.out.println("\n" + "=".repeat(70));
    }

    @Test
    @DisplayName("DOM에서 직접 다운로드 URL 추출 (가장 확실한 방법)")
    void testExtractFromDOM() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 DOM 직접 추출 테스트");
        System.out.println("=".repeat(70));

        // Step 1: 페이지 로드
        System.out.println("\n📍 Step 1: sssinstagram.com 로드...");
        long startTime = System.currentTimeMillis();
        driver.get("https://sssinstagram.com");
        Thread.sleep(2000);
        System.out.println("✅ 페이지 로드 완료");

        // Step 2: Instagram URL 입력
        System.out.println("\n📍 Step 2: Instagram URL 입력...");
        WebElement urlInput = driver.findElement(By.cssSelector("input"));
        urlInput.sendKeys(TEST_URL);
        System.out.println("✅ URL 입력 완료");

        // Step 3: 다운로드 버튼 클릭
        System.out.println("\n📍 Step 3: 다운로드 버튼 클릭...");
        WebElement downloadButton = driver.findElement(By.cssSelector("button"));
        downloadButton.click();
        System.out.println("✅ 다운로드 버튼 클릭 완료");

        // Step 4: 다운로드 링크 대기 및 추출
        System.out.println("\n📍 Step 4: 다운로드 링크 대기...");

        // 여러 selector 시도
        String[] selectors = {
            "a[download]",
            "a[href*='cdninstagram']",
            "a[href*='instagram']",
            "a[href*='.jpg']",
            "a[href*='.mp4']",
            ".download-link",
            "#download-link",
            "div[class*='download'] a",
            "div[class*='result'] a"
        };

        WebElement downloadLink = null;
        String foundSelector = null;

        for (String selector : selectors) {
            try {
                downloadLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(selector)
                ));
                foundSelector = selector;
                break;
            } catch (Exception e) {
                // 다음 selector 시도
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        if (downloadLink == null) {
            System.err.println("❌ 다운로드 링크를 찾지 못했습니다");
            System.err.println("💡 페이지 HTML 구조 확인 필요");

            // 페이지 소스 일부 출력
            String pageSource = driver.getPageSource();
            System.out.println("\n📄 페이지 소스 (처음 500자):");
            System.out.println(pageSource.substring(0, Math.min(500, pageSource.length())));

            fail("다운로드 링크를 찾지 못했습니다");
        }

        String downloadUrl = downloadLink.getAttribute("href");

        System.out.println("\n🎉 다운로드 URL 추출 성공!");
        System.out.println("  Selector: " + foundSelector);
        System.out.println("  URL: " + downloadUrl);
        System.out.println("  소요 시간: " + elapsed + "ms");

        assertNotNull(downloadUrl);
        assertTrue(downloadUrl.startsWith("http"), "유효한 URL이어야 합니다");

        System.out.println("\n✅ 테스트 성공!");
        System.out.println("=".repeat(70));
    }
}
