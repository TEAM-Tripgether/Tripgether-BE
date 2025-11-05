package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.RequestId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chrome DevTools Protocol로 네트워크 응답 캡처
 *
 * 전략:
 * 1. Selenium으로 다운로드 버튼 클릭
 * 2. DevTools로 /api/convert 응답 캡처
 * 3. 응답 JSON에서 다운로드 URL 추출
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetworkCaptureInstagramTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    private ChromeDriver driver;
    private DevTools devTools;
    private WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        System.out.println("=".repeat(70));
        System.out.println("🚀 네트워크 캡처 테스트 초기화");
        System.out.println("=".repeat(70));

        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료\n");
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // DevTools 사용을 위해 headless 모드 OFF
        // options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // DevTools 초기화
        devTools = driver.getDevTools();
        devTools.createSession();
    }

    @AfterEach
    void tearDown() {
        if (devTools != null) {
            devTools.close();
        }
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("DevTools로 /api/convert 응답 캡처")
    void testCaptureApiResponse() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 네트워크 응답 캡처 테스트");
        System.out.println("=".repeat(70));

        // CompletableFuture로 응답 캡처
        CompletableFuture<String> apiResponseFuture = new CompletableFuture<>();

        // Step 1: Network 도메인 활성화
        System.out.println("\n📍 Step 1: DevTools Network 활성화...");
        devTools.send(Network.enable(
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        ));
        System.out.println("✅ Network 감시 시작");

        // Step 2: 응답 리스너 등록
        System.out.println("\n📍 Step 2: 응답 리스너 등록...");
        devTools.addListener(Network.responseReceived(), response -> {
            String url = response.getResponse().getUrl();
            int statusCode = response.getResponse().getStatus();

            if (url.contains("/api/convert")) {
                System.out.println("\n🎯 API 요청 감지!");
                System.out.println("  URL: " + url);
                System.out.println("  상태 코드: " + statusCode);

                if (statusCode == 200) {
                    RequestId requestId = response.getRequestId();

                    try {
                        // Response body 가져오기
                        Network.GetResponseBodyResponse responseBody =
                            devTools.send(Network.getResponseBody(requestId));

                        String body = responseBody.getBody();
                        System.out.println("  응답 본문: " + body);

                        apiResponseFuture.complete(body);
                    } catch (Exception e) {
                        System.err.println("❌ 응답 본문 가져오기 실패: " + e.getMessage());
                        apiResponseFuture.completeExceptionally(e);
                    }
                }
            }
        });
        System.out.println("✅ 리스너 등록 완료");

        // Step 3: 페이지 로드
        System.out.println("\n📍 Step 3: sssinstagram.com 로드...");
        driver.get("https://sssinstagram.com");
        Thread.sleep(2000);
        System.out.println("✅ 페이지 로드 완료");

        // Step 4: Instagram URL 입력
        System.out.println("\n📍 Step 4: Instagram URL 입력...");
        WebElement urlInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='text'], input[placeholder*='URL'], input[placeholder*='url'], input[name='url']")
        ));
        urlInput.clear();
        urlInput.sendKeys(TEST_URL);
        System.out.println("✅ URL 입력 완료");

        // Step 5: 다운로드 버튼 클릭
        System.out.println("\n📍 Step 5: 다운로드 버튼 클릭...");
        WebElement downloadButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit'], button")
        ));
        downloadButton.click();
        System.out.println("✅ 다운로드 버튼 클릭 완료");

        // Step 6: API 응답 대기
        System.out.println("\n📍 Step 6: API 응답 대기 (최대 30초)...");
        String apiResponse = apiResponseFuture.get(30, TimeUnit.SECONDS);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ API 응답 캡처 성공!");
        System.out.println("=".repeat(70));

        // Step 7: 응답 검증 및 파싱
        assertNotNull(apiResponse, "API 응답이 null입니다");
        assertFalse(apiResponse.isEmpty(), "API 응답이 비어있습니다");

        System.out.println("\n📥 캡처된 API 응답:");
        System.out.println(apiResponse);

        // JSON 파싱
        JsonObject jsonResponse = JsonParser.parseString(apiResponse).getAsJsonObject();

        if (jsonResponse.has("error")) {
            String error = jsonResponse.get("error").getAsString();
            fail("API 에러 응답: " + error);
        }

        // 다운로드 URL 추출 (실제 응답 구조에 따라 조정 필요)
        if (jsonResponse.has("url")) {
            String downloadUrl = jsonResponse.get("url").getAsString();
            System.out.println("\n🎉 다운로드 URL 추출 성공:");
            System.out.println("  " + downloadUrl);

            assertNotNull(downloadUrl);
            assertTrue(downloadUrl.startsWith("http"), "유효한 URL이어야 합니다");
        } else {
            System.out.println("\n📋 전체 응답 구조:");
            System.out.println(new Gson().toJson(jsonResponse));
        }

        System.out.println("\n" + "=".repeat(70));
    }

    @Test
    @DisplayName("네트워크 캡처 성능 테스트")
    void testCapturePerformance() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⚡ 네트워크 캡처 성능 테스트");
        System.out.println("=".repeat(70));

        int iterations = 3;
        long totalTime = 0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("\n🔄 시도 " + i + "/" + iterations + ":");

            long startTime = System.currentTimeMillis();

            // DevTools 활성화
            devTools.send(Network.enable(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            ));

            CompletableFuture<String> responseFuture = new CompletableFuture<>();

            devTools.addListener(Network.responseReceived(), response -> {
                if (response.getResponse().getUrl().contains("/api/convert") &&
                    response.getResponse().getStatus() == 200) {

                    try {
                        Network.GetResponseBodyResponse body =
                            devTools.send(Network.getResponseBody(response.getRequestId()));
                        responseFuture.complete(body.getBody());
                    } catch (Exception e) {
                        responseFuture.completeExceptionally(e);
                    }
                }
            });

            // 페이지 로드
            driver.get("https://sssinstagram.com");
            Thread.sleep(1000);

            // URL 입력 및 버튼 클릭
            WebElement input = driver.findElement(By.cssSelector("input"));
            input.sendKeys(TEST_URL);

            WebElement button = driver.findElement(By.cssSelector("button"));
            button.click();

            // 응답 대기
            String response = responseFuture.get(30, TimeUnit.SECONDS);

            long elapsed = System.currentTimeMillis() - startTime;
            totalTime += elapsed;

            System.out.println("  응답 길이: " + response.length() + " chars");
            System.out.println("  소요 시간: " + elapsed + "ms");

            assertNotNull(response);
            assertFalse(response.isEmpty());

            Thread.sleep(1000); // 요청 간 간격
        }

        double avgTime = totalTime / (double) iterations;
        System.out.println("\n📊 평균 소요 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("=".repeat(70));

        // Selenium 기반이므로 3-5초 예상
        assertTrue(avgTime > 1000, "최소 1초 이상 소요");
        assertTrue(avgTime < 10000, "10초 이내 완료");
    }
}
