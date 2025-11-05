package com.tripgether.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Selenium 테스트: sssinstagram.com API 테스트
 * Chrome 브라우저를 실제로 띄워서 Ajax 요청과 쿠키를 캡처합니다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SSSInstagramSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private Gson gson;

    private static final String TARGET_URL = "https://sssinstagram.com/ko";
    private static final String INSTAGRAM_POST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    @BeforeAll
    void setupClass() {
        System.out.println("=================================================");
        System.out.println("🚀 Selenium 테스트 초기화: sssinstagram.com");
        System.out.println("=================================================\n");

        // WebDriverManager를 사용하여 ChromeDriver 자동 설정
        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료");

        // Gson 초기화
        gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("✅ Gson 초기화 완료\n");
    }

    @BeforeEach
    void setup() {
        // Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();

        // 실제 브라우저를 띄워서 확인 (headless 모드 비활성화)
        // options.addArguments("--headless"); // 헤드리스 모드 사용 시 주석 해제

        // 브라우저 로그 수집 활성화 (Performance 로그 포함)
        options.setCapability("goog:loggingPrefs",
            java.util.Map.of("browser", "ALL", "performance", "ALL"));

        // 추가 옵션
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36");

        // 언어 설정
        options.addArguments("--lang=ko-KR");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // WebDriverWait 설정 (최대 30초 대기)
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        System.out.println("🌐 Chrome 브라우저 시작\n");
    }

    @Test
    @DisplayName("sssinstagram.com Instagram 다운로더 전체 플로우 테스트")
    void testSSSInstagramDownloader() throws InterruptedException {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔄 전체 플로우 테스트 시작");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Step 1: 페이지 접속
        System.out.println("📍 Step 1: 페이지 접속");
        System.out.println("   URL: " + TARGET_URL);
        driver.get(TARGET_URL);
        Thread.sleep(3000); // 페이지 로딩 및 초기 스크립트 실행 대기
        System.out.println("   ✅ 페이지 로딩 완료\n");

        // Step 2: 초기 쿠키 확인
        System.out.println("📍 Step 2: 초기 쿠키 확인");
        logCookies("초기 상태");

        // Step 3: 입력 필드 찾기 및 URL 입력
        System.out.println("\n📍 Step 3: Instagram URL 입력");
        System.out.println("   찾는 요소: input (여러 선택자 시도)");

        // 여러 선택자로 시도
        WebElement inputField = null;
        String[] selectors = {
            "input#input",
            "input.form__input",
            "input[type='text']",
            "input[placeholder*='링크']",
            "input[placeholder*='link']",
            "input[placeholder*='URL']"
        };

        for (String selector : selectors) {
            try {
                System.out.println("   시도: " + selector);
                inputField = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector))
                );
                System.out.println("   ✅ 입력 필드 발견: " + selector);
                break;
            } catch (Exception e) {
                System.out.println("   ⚠️  실패: " + selector);
            }
        }

        if (inputField == null) {
            System.out.println("   ❌ 입력 필드를 찾을 수 없습니다. 페이지 HTML 확인 필요");
            System.out.println("\n   📄 페이지 소스 (처음 500자):");
            String pageSource = driver.getPageSource();
            System.out.println(pageSource.substring(0, Math.min(500, pageSource.length())));
            Assertions.fail("입력 필드를 찾을 수 없습니다");
        }

        System.out.println("   ✅ 입력 필드 발견");
        System.out.println("   입력 URL: " + INSTAGRAM_POST_URL);

        inputField.clear();
        inputField.sendKeys(INSTAGRAM_POST_URL);
        Thread.sleep(1000);
        System.out.println("   ✅ URL 입력 완료\n");

        // Step 4: 다운로드 버튼 클릭
        System.out.println("📍 Step 4: 다운로드 버튼 클릭");
        System.out.println("   찾는 요소: button.form__submit");

        WebElement downloadButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.form__submit"))
        );

        System.out.println("   ✅ 다운로드 버튼 발견");
        System.out.println("   🖱️  버튼 클릭 중...");
        downloadButton.click();
        System.out.println("   ✅ 버튼 클릭 완료\n");

        // Step 5: API 응답 대기
        System.out.println("📍 Step 5: API 응답 대기");
        System.out.println("   예상 API: /msec, /api/convert");
        Thread.sleep(5000); // API 응답 대기

        // Step 6: 결과 확인 (다운로드 링크 등)
        System.out.println("\n📍 Step 6: 다운로드 결과 확인");
        try {
            // 다운로드 링크가 포함된 요소 찾기 (다양한 선택자 시도)
            List<WebElement> downloadLinks = driver.findElements(By.cssSelector("a[href*='http'], a[download]"));

            if (!downloadLinks.isEmpty()) {
                System.out.println("   ✅ 다운로드 링크 발견: " + downloadLinks.size() + "개");

                int count = 0;
                for (WebElement link : downloadLinks) {
                    String href = link.getAttribute("href");
                    String text = link.getText();

                    if (href != null && (href.contains("cdn") || href.contains("media") || href.contains("content"))) {
                        count++;
                        System.out.println("   " + count + ". [" + text + "] " + href);

                        if (count >= 5) break; // 처음 5개만 출력
                    }
                }
            } else {
                System.out.println("   ⚠️  다운로드 링크를 찾을 수 없습니다.");
            }

            // 결과 영역 전체 텍스트 출력
            List<WebElement> resultContainers = driver.findElements(
                By.cssSelector(".download, .result, .media, [class*='download'], [class*='result']")
            );

            if (!resultContainers.isEmpty()) {
                System.out.println("\n   📦 결과 컨테이너 내용:");
                for (WebElement container : resultContainers) {
                    String text = container.getText();
                    if (!text.isEmpty() && text.length() < 500) {
                        System.out.println("   " + text);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("   ⚠️  결과 파싱 중 예외 발생: " + e.getMessage());
        }

        // Step 7: 쿠키 변화 확인
        System.out.println("\n📍 Step 7: 최종 쿠키 상태");
        logCookies("최종 상태");

        // Step 8: 네트워크 로그 분석 (Ajax 요청)
        System.out.println("\n📍 Step 8: 네트워크 로그 분석 (Ajax 요청 캡처)");
        analyzeNetworkLogs();

        // Step 9: 브라우저 콘솔 로그
        System.out.println("\n📍 Step 9: 브라우저 콘솔 로그");
        captureBrowserLogs();

        // 스크린샷 대기 (수동 확인용)
        System.out.println("\n⏱️  5초간 대기 (수동 확인 가능)...");
        Thread.sleep(5000);

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ 테스트 완료");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    /**
     * 현재 쿠키 상태 로깅
     */
    private void logCookies(String stage) {
        Set<Cookie> cookies = driver.manage().getCookies();

        if (cookies.isEmpty()) {
            System.out.println("   🍪 쿠키: (없음)");
        } else {
            System.out.println("   🍪 쿠키 (" + stage + "): " + cookies.size() + "개");

            for (Cookie cookie : cookies) {
                System.out.println("      - " + cookie.getName() + " = " + cookie.getValue());
                System.out.println("        도메인: " + cookie.getDomain() +
                                 ", 경로: " + cookie.getPath() +
                                 ", 보안: " + cookie.isSecure() +
                                 ", HttpOnly: " + cookie.isHttpOnly());
            }
        }
    }

    /**
     * 네트워크 로그를 분석하여 Ajax 요청 추출
     */
    private void analyzeNetworkLogs() {
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
            List<LogEntry> allLogs = logEntries.getAll();

            System.out.println("📊 네트워크 로그 분석:");
            System.out.println("   총 로그 수: " + allLogs.size());

            // Ajax 관련 요청 필터링
            List<NetworkRequest> ajaxRequests = new ArrayList<>();

            for (LogEntry entry : allLogs) {
                String message = entry.getMessage();

                // JSON 파싱 시도
                try {
                    JsonObject logJson = gson.fromJson(message, JsonObject.class);
                    JsonObject messageObj = logJson.getAsJsonObject("message");

                    if (messageObj == null) continue;

                    String method = messageObj.get("method").getAsString();
                    JsonObject params = messageObj.getAsJsonObject("params");

                    // 요청 전송 감지
                    if ("Network.requestWillBeSent".equals(method) && params != null) {
                        JsonObject request = params.getAsJsonObject("request");
                        if (request != null) {
                            String url = request.get("url").getAsString();
                            String httpMethod = request.get("method").getAsString();

                            // Ajax 요청 필터링 (XHR, Fetch, API 엔드포인트)
                            if (url.contains("/api/") || url.contains("/msec") ||
                                url.contains("convert") || url.contains("ajax") ||
                                "POST".equals(httpMethod)) {

                                NetworkRequest req = new NetworkRequest();
                                req.url = url;
                                req.method = httpMethod;
                                req.requestId = params.has("requestId") ?
                                    params.get("requestId").getAsString() : "";

                                // 요청 헤더
                                if (request.has("headers")) {
                                    req.headers = request.getAsJsonObject("headers");
                                }

                                // POST 데이터
                                if (request.has("postData")) {
                                    req.postData = request.get("postData").getAsString();
                                }

                                ajaxRequests.add(req);
                            }
                        }
                    }

                    // 응답 수신 감지
                    if ("Network.responseReceived".equals(method) && params != null) {
                        String requestId = params.has("requestId") ?
                            params.get("requestId").getAsString() : "";
                        JsonObject response = params.getAsJsonObject("response");

                        if (response != null) {
                            String url = response.get("url").getAsString();
                            int status = response.get("status").getAsInt();

                            // 매칭되는 요청 찾기
                            for (NetworkRequest req : ajaxRequests) {
                                if (req.requestId.equals(requestId) || req.url.equals(url)) {
                                    req.statusCode = status;
                                    req.responseHeaders = response.has("headers") ?
                                        response.getAsJsonObject("headers") : null;
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    // JSON 파싱 실패 시 무시
                }
            }

            // Ajax 요청 출력
            if (ajaxRequests.isEmpty()) {
                System.out.println("   ⚠️  Ajax 요청을 찾을 수 없습니다.");
            } else {
                System.out.println("   ✅ Ajax 요청 " + ajaxRequests.size() + "개 발견\n");

                int count = 0;
                for (NetworkRequest req : ajaxRequests) {
                    count++;
                    System.out.println("   🔹 Ajax 요청 #" + count);
                    System.out.println("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    System.out.println("   URL: " + req.url);
                    System.out.println("   메서드: " + req.method);
                    System.out.println("   상태 코드: " + (req.statusCode > 0 ? req.statusCode : "대기 중"));

                    // 요청 헤더 출력
                    if (req.headers != null) {
                        System.out.println("\n   📋 요청 헤더:");
                        for (Map.Entry<String, JsonElement> entry : req.headers.entrySet()) {
                            System.out.println("      " + entry.getKey() + ": " +
                                entry.getValue().getAsString());
                        }
                    }

                    // POST 데이터 출력
                    if (req.postData != null && !req.postData.isEmpty()) {
                        System.out.println("\n   📦 요청 본문:");
                        try {
                            // JSON 파싱 시도
                            JsonObject postJson = gson.fromJson(req.postData, JsonObject.class);
                            System.out.println("   " + gson.toJson(postJson));
                        } catch (Exception e) {
                            // JSON이 아닌 경우 그대로 출력
                            System.out.println("   " + req.postData);
                        }
                    }

                    // 응답 헤더 출력
                    if (req.responseHeaders != null) {
                        System.out.println("\n   📋 응답 헤더:");
                        for (Map.Entry<String, JsonElement> entry : req.responseHeaders.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue().getAsString();

                            // 중요한 헤더만 출력
                            if (key.equalsIgnoreCase("content-type") ||
                                key.equalsIgnoreCase("set-cookie") ||
                                key.equalsIgnoreCase("access-control-allow-origin")) {
                                System.out.println("      " + key + ": " + value);
                            }
                        }
                    }

                    System.out.println();
                }
            }

        } catch (Exception e) {
            System.out.println("   ❌ 네트워크 로그 분석 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 브라우저 콘솔 로그 캡처
     */
    private void captureBrowserLogs() {
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            List<LogEntry> logs = logEntries.getAll();

            if (logs.isEmpty()) {
                System.out.println("   ℹ️  브라우저 콘솔 로그가 없습니다.");
                return;
            }

            System.out.println("   총 콘솔 로그 수: " + logs.size());
            System.out.println("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // 중요한 로그만 필터링 (에러, 경고)
            List<LogEntry> importantLogs = logs.stream()
                .filter(log -> log.getLevel() == Level.SEVERE || log.getLevel() == Level.WARNING)
                .collect(Collectors.toList());

            if (importantLogs.isEmpty()) {
                System.out.println("   ✅ 에러 또는 경고 없음");
            } else {
                for (LogEntry entry : importantLogs) {
                    String level = entry.getLevel().getName();
                    String emoji = getLogEmoji(entry.getLevel());

                    System.out.println("\n   " + emoji + " [" + level + "]");
                    System.out.println("   시간: " + new Date(entry.getTimestamp()));
                    System.out.println("   메시지: " + entry.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("   ❌ 브라우저 로그 수집 실패: " + e.getMessage());
        }
    }

    /**
     * 로그 레벨에 따른 이모지 반환
     */
    private String getLogEmoji(Level level) {
        if (level == Level.SEVERE) return "🔴";
        if (level == Level.WARNING) return "🟡";
        if (level == Level.INFO) return "🔵";
        return "⚪";
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            System.out.println("\n🛑 브라우저 종료");
            driver.quit();
        }
    }

    /**
     * 네트워크 요청 정보를 담는 내부 클래스
     */
    private static class NetworkRequest {
        String url;
        String method;
        String requestId;
        int statusCode;
        JsonObject headers;
        JsonObject responseHeaders;
        String postData;
    }
}
