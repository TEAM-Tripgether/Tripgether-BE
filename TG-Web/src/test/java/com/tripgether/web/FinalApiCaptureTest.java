package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XHR 후킹으로 API 응답 직접 캡처 (가장 확실한 방법)
 *
 * 전략:
 * 1. Selenium으로 페이지 로드
 * 2. JavaScript로 XMLHttpRequest 후킹
 * 3. 다운로드 버튼 클릭
 * 4. 캡처된 /api/convert 응답 추출
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinalApiCaptureTest {

    private static final String TEST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    private ChromeDriver driver;

    @BeforeAll
    void setupClass() {
        System.out.println("=".repeat(70));
        System.out.println("🚀 XHR 후킹 API 응답 캡처 테스트");
        System.out.println("=".repeat(70));

        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료\n");
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Headless 모드 OFF (디버깅)
        // options.addArguments("--headless");
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
    @DisplayName("XHR 후킹으로 /api/convert 응답 캡처")
    void testCaptureApiResponse() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔐 API 응답 캡처 테스트");
        System.out.println("=".repeat(70));

        // Step 1: 페이지 로드
        System.out.println("\n📍 Step 1: sssinstagram.com 로드...");
        long startTime = System.currentTimeMillis();
        driver.get("https://sssinstagram.com/ko");
        Thread.sleep(3000); // 페이지 완전 로드 대기
        System.out.println("✅ 페이지 로드 완료");

        // Step 2: XHR 후킹 스크립트 주입
        System.out.println("\n📍 Step 2: XMLHttpRequest 후킹...");
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String xhrHookScript = """
            (function() {
                window.capturedApiResponse = null;

                // XMLHttpRequest 후킹
                const originalOpen = XMLHttpRequest.prototype.open;
                const originalSend = XMLHttpRequest.prototype.send;

                XMLHttpRequest.prototype.open = function(method, url) {
                    this._url = url;
                    this._method = method;
                    return originalOpen.apply(this, arguments);
                };

                XMLHttpRequest.prototype.send = function(body) {
                    const xhr = this;

                    // 응답 리스너
                    xhr.addEventListener('load', function() {
                        if (xhr._url && xhr._url.includes('/api/convert')) {
                            console.log('🎯 API 응답 캡처!');
                            console.log('URL:', xhr._url);
                            console.log('Status:', xhr.status);
                            console.log('Response:', xhr.responseText);

                            // 전역 변수에 저장
                            window.capturedApiResponse = {
                                url: xhr._url,
                                status: xhr.status,
                                response: xhr.responseText
                            };
                        }
                    });

                    return originalSend.apply(this, arguments);
                };

                // Fetch API 후킹 (혹시 Fetch를 사용할 경우)
                const originalFetch = window.fetch;
                window.fetch = function(...args) {
                    return originalFetch.apply(this, args).then(response => {
                        const url = args[0];

                        if (typeof url === 'string' && url.includes('/api/convert')) {
                            return response.clone().text().then(text => {
                                console.log('🎯 Fetch API 응답 캡처!');
                                console.log('URL:', url);
                                console.log('Status:', response.status);
                                console.log('Response:', text);

                                window.capturedApiResponse = {
                                    url: url,
                                    status: response.status,
                                    response: text
                                };

                                return response;
                            });
                        }

                        return response;
                    });
                };

                console.log('✅ XHR/Fetch 후킹 완료!');
            })();
            """;

        js.executeScript(xhrHookScript);
        System.out.println("✅ XHR 후킹 완료");

        // Step 3: Instagram URL 입력
        System.out.println("\n📍 Step 3: Instagram URL 입력...");
        WebElement urlInput = driver.findElement(By.cssSelector("input[type='text'], input[type='url'], input.form__input"));
        urlInput.clear();
        urlInput.sendKeys(TEST_URL);
        System.out.println("✅ URL 입력 완료");

        // 잠시 대기 (페이지가 입력을 처리할 시간)
        Thread.sleep(500);

        // Step 4: 다운로드 버튼 클릭 - 정확한 셀렉터 사용
        System.out.println("\n📍 Step 4: 다운로드 버튼 클릭...");
        WebElement downloadButton = driver.findElement(By.cssSelector("button.form__submit[type='submit']"));

        // JavaScript로 클릭 (더 안정적)
        js.executeScript("arguments[0].click();", downloadButton);
        System.out.println("✅ 다운로드 버튼 클릭 완료");

        // Step 5: API 응답 대기 및 추출
        System.out.println("\n📍 Step 5: API 응답 대기...");

        // 최대 60초 대기
        String apiResponse = null;
        for (int i = 0; i < 60; i++) {
            Thread.sleep(1000);

            Object captured = js.executeScript("return window.capturedApiResponse;");
            if (captured != null) {
                // Java Map으로 변환
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) captured;

                Long status = (Long) responseMap.get("status");
                String response = (String) responseMap.get("response");

                System.out.println("  ⏱️  " + (i + 1) + "초: API 응답 캡처됨!");
                System.out.println("  상태 코드: " + status);

                if (status == 200 && response != null && !response.isEmpty()) {
                    apiResponse = response;
                    break;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("\n" + "=".repeat(70));

        if (apiResponse == null) {
            fail("API 응답을 캡처하지 못했습니다 (60초 타임아웃)");
        }

        System.out.println("✅ API 응답 캡처 성공!");
        System.out.println("=".repeat(70));

        // Step 6: 응답 검증 및 파싱
        System.out.println("\n📥 캡처된 API 응답:");
        System.out.println(apiResponse);
        System.out.println();

        assertNotNull(apiResponse, "API 응답이 null입니다");
        assertFalse(apiResponse.isEmpty(), "API 응답이 비어있습니다");

        // JSON 파싱 - 배열로 파싱
        JsonArray jsonResponse = JsonParser.parseString(apiResponse).getAsJsonArray();

        // 응답 구조 출력
        System.out.println("📋 캡처된 API 응답 (총 " + jsonResponse.size() + "개 항목):");
        System.out.println(new Gson().toJson(jsonResponse));
        System.out.println();

        // 첫 번째 항목 분석
        if (jsonResponse.size() > 0) {
            JsonObject firstItem = jsonResponse.get(0).getAsJsonObject();

            System.out.println("🎉 API 응답 데이터:");

            // URL 배열 추출
            if (firstItem.has("url")) {
                JsonArray urls = firstItem.getAsJsonArray("url");
                System.out.println("\n📎 다운로드 URL (" + urls.size() + "개):");
                for (int i = 0; i < urls.size(); i++) {
                    JsonObject urlObj = urls.get(i).getAsJsonObject();
                    String url = urlObj.get("url").getAsString();
                    String name = urlObj.get("name").getAsString();
                    String type = urlObj.get("type").getAsString();
                    System.out.println("  " + (i + 1) + ". " + name + " (" + type + "): " + url);

                    assertNotNull(url);
                    assertTrue(url.startsWith("http"), "유효한 URL이어야 합니다");
                }
            }

            // 메타데이터 추출
            if (firstItem.has("meta")) {
                JsonObject meta = firstItem.getAsJsonObject("meta");
                System.out.println("\n📊 메타데이터:");
                System.out.println("  제목: " + meta.get("title").getAsString());
                System.out.println("  사용자: " + meta.get("username").getAsString());
                System.out.println("  좋아요: " + meta.get("like_count").getAsInt());
                System.out.println("  댓글: " + meta.get("comment_count").getAsInt());
            }

            // 썸네일
            if (firstItem.has("thumb")) {
                System.out.println("\n🖼️  썸네일: " + firstItem.get("thumb").getAsString());
            }
        }

        System.out.println("\n⏱️  총 소요 시간: " + elapsed + "ms");
        System.out.println("=".repeat(70));

        // 성능 검증: 합리적인 시간 내 완료 (30초 이내)
        assertTrue(elapsed < 30000, "30초 이내 완료되어야 합니다");
    }

    @Test
    @DisplayName("성능 벤치마크 - 3회 반복")
    void testPerformanceBenchmark() throws Exception {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⚡ 성능 벤치마크");
        System.out.println("=".repeat(70));

        int iterations = 3;
        long totalTime = 0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("\n🔄 시도 " + i + "/" + iterations + ":");

            long startTime = System.currentTimeMillis();

            // 테스트 실행
            driver.get("https://sssinstagram.com");
            Thread.sleep(1000);

            // XHR 후킹
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String xhrHookScript = """
                window.capturedApiResponse = null;
                const originalOpen = XMLHttpRequest.prototype.open;
                const originalSend = XMLHttpRequest.prototype.send;
                XMLHttpRequest.prototype.open = function(method, url) {
                    this._url = url;
                    return originalOpen.apply(this, arguments);
                };
                XMLHttpRequest.prototype.send = function(body) {
                    const xhr = this;
                    xhr.addEventListener('load', function() {
                        if (xhr._url && xhr._url.includes('/api/convert')) {
                            window.capturedApiResponse = { status: xhr.status, response: xhr.responseText };
                        }
                    });
                    return originalSend.apply(this, arguments);
                };
                """;
            js.executeScript(xhrHookScript);

            // URL 입력 및 버튼 클릭
            WebElement input = driver.findElement(By.cssSelector("input"));
            input.sendKeys(TEST_URL);

            WebElement button = driver.findElement(By.cssSelector("button"));
            button.click();

            // 응답 대기
            String apiResponse = null;
            for (int j = 0; j < 30; j++) {
                Thread.sleep(1000);
                Object captured = js.executeScript("return window.capturedApiResponse;");
                if (captured != null) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) captured;
                    apiResponse = (String) responseMap.get("response");
                    if (apiResponse != null) break;
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            totalTime += elapsed;

            System.out.println("  응답 길이: " + (apiResponse != null ? apiResponse.length() : 0) + " chars");
            System.out.println("  소요 시간: " + elapsed + "ms");

            assertNotNull(apiResponse, "시도 " + i + " 실패");

            Thread.sleep(1000); // 요청 간 간격
        }

        double avgTime = totalTime / (double) iterations;
        System.out.println("\n📊 평균 소요 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("=".repeat(70));

        assertTrue(avgTime < 15000, "평균 15초 이내 완료");
    }
}
