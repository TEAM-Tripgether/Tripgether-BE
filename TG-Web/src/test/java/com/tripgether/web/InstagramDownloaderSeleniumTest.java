package com.tripgether.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
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
import java.util.List;
import java.util.logging.Level;

/**
 * Selenium 테스트: Instagram 다운로더 페이지 자동화 테스트
 * Chrome 브라우저를 실제로 띄워서 페이지 동작을 확인합니다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstagramDownloaderSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String TARGET_URL = "https://saveclip.app/ko/download-video-instagram";
    private static final String INSTAGRAM_POST_URL = "https://www.instagram.com/p/DQHEMGPEqWK/?img_index=3&igsh=MWU5YTM3b2JpZzJp";

    @BeforeAll
    void setupClass() {
        // WebDriverManager를 사용하여 ChromeDriver 자동 설정
        WebDriverManager.chromedriver().setup();
        System.out.println("✅ ChromeDriver 설정 완료");
    }

    @BeforeEach
    void setup() {
        // Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();

        // 실제 브라우저를 띄워서 확인 (headless 모드 비활성화)
        // options.addArguments("--headless"); // 헤드리스 모드 사용 시 주석 해제

        // 브라우저 로그 수집 활성화
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

        // WebDriverWait 설정 (최대 20초 대기)
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        System.out.println("🌐 Chrome 브라우저 시작");
    }

    @Test
    @DisplayName("Instagram 다운로더 페이지 테스트 - 실제 Chrome 브라우저 사용")
    void testInstagramDownloader() throws InterruptedException {
        System.out.println("\n=================================================");
        System.out.println("🚀 테스트 시작: Instagram 다운로더 자동화");
        System.out.println("=================================================\n");

        // 1. 페이지 이동
        System.out.println("📍 Step 1: 페이지 접속");
        System.out.println("   URL: " + TARGET_URL);
        driver.get(TARGET_URL);
        Thread.sleep(2000); // 페이지 로딩 대기
        System.out.println("   ✅ 페이지 로딩 완료\n");

        // 2. 입력 필드 찾기 및 URL 입력
        System.out.println("📍 Step 2: Instagram URL 입력");
        System.out.println("   찾는 요소: input[name='q']#s_input");

        WebElement inputField = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='q']#s_input"))
        );

        System.out.println("   ✅ 입력 필드 발견");
        System.out.println("   입력 URL: " + INSTAGRAM_POST_URL);

        inputField.clear();
        inputField.sendKeys(INSTAGRAM_POST_URL);
        Thread.sleep(1000);
        System.out.println("   ✅ URL 입력 완료\n");

        // 3. 다운로드 버튼 클릭
        System.out.println("📍 Step 3: 다운로드 버튼 클릭");
        System.out.println("   찾는 요소: button with onclick containing 'ksearchvideo'");

        // 여러 방법으로 버튼 찾기 시도
        WebElement downloadButton = null;
        try {
            // 방법 1: XPath로 onclick 속성 기반 검색
            downloadButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@onclick, 'ksearchvideo')]")
                )
            );
            System.out.println("   ✅ 다운로드 버튼 발견 (XPath)");
        } catch (Exception e) {
            // 방법 2: CSS selector로 텍스트 기반 검색
            downloadButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), '다운로드')]")
                )
            );
            System.out.println("   ✅ 다운로드 버튼 발견 (텍스트)");
        }

        System.out.println("   🖱️  버튼 클릭 중...");
        downloadButton.click();
        System.out.println("   ✅ 버튼 클릭 완료\n");

        // 4. API 요청 대기 및 응답 확인
        System.out.println("📍 Step 4: API 응답 대기");
        System.out.println("   예상 요청 URL: https://v3.saveclip.app/api/ajaxSearch");
        Thread.sleep(5000); // API 응답 대기

        // 5. 결과 확인
        System.out.println("\n📍 Step 5: 결과 확인");
        try {
            // 다운로드 박스가 나타날 때까지 대기
            WebElement downloadBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("ul.download-box")
                )
            );
            System.out.println("   ✅ 다운로드 결과 박스 발견");
            System.out.println("   내용:\n" + downloadBox.getText());
        } catch (Exception e) {
            System.out.println("   ⚠️  다운로드 박스를 찾을 수 없습니다: " + e.getMessage());
        }

        // 6. 브라우저 네트워크 로그 수집
        System.out.println("\n📍 Step 6: 네트워크 로그 분석");
        captureNetworkLogs();

        // 7. 브라우저 콘솔 로그 수집
        System.out.println("\n📍 Step 7: 브라우저 콘솔 로그");
        captureBrowserLogs();

        // 스크린샷 대기 (수동 확인용)
        System.out.println("\n⏱️  5초간 대기 (수동 확인 가능)...");
        Thread.sleep(5000);

        System.out.println("\n=================================================");
        System.out.println("✅ 테스트 완료");
        System.out.println("=================================================\n");
    }

    /**
     * 네트워크 로그 캡처 및 분석
     */
    private void captureNetworkLogs() {
        try {
            LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);

            System.out.println("📊 네트워크 로그:");
            System.out.println("   총 로그 수: " + logEntries.getAll().size());

            int apiCallCount = 0;
            for (LogEntry entry : logEntries) {
                String message = entry.getMessage();

                // ajaxSearch API 호출 관련 로그만 필터링
                if (message.contains("ajaxSearch") ||
                    message.contains("v3.saveclip.app")) {

                    apiCallCount++;
                    System.out.println("\n   🔍 API 호출 발견 #" + apiCallCount);
                    System.out.println("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

                    // JSON 메시지 파싱 시도
                    if (message.contains("\"method\":")) {
                        try {
                            // method 추출
                            if (message.contains("\"method\":\"Network.requestWillBeSent\"")) {
                                System.out.println("   타입: 요청 전송");
                                if (message.contains("\"url\":")) {
                                    String url = extractJsonValue(message, "url");
                                    System.out.println("   URL: " + url);
                                }
                                if (message.contains("\"method\":\"POST\"")) {
                                    System.out.println("   HTTP Method: POST");
                                }
                            }

                            if (message.contains("\"method\":\"Network.responseReceived\"")) {
                                System.out.println("   타입: 응답 수신");
                                if (message.contains("\"status\":")) {
                                    String status = extractJsonValue(message, "status");
                                    System.out.println("   상태 코드: " + status);
                                }
                            }

                            if (message.contains("\"method\":\"Network.requestWillBeSentExtraInfo\"")) {
                                System.out.println("   타입: 요청 추가 정보");
                                // 헤더 정보 출력
                                if (message.contains("\"headers\":")) {
                                    System.out.println("   헤더 정보 포함됨");
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("   ⚠️  JSON 파싱 실패");
                        }
                    }

                    // 전체 메시지 출력 (디버깅용)
                    System.out.println("\n   📝 전체 메시지:");
                    System.out.println("   " + message.substring(0, Math.min(message.length(), 500)) + "...");
                }
            }

            if (apiCallCount == 0) {
                System.out.println("   ⚠️  ajaxSearch API 호출 로그를 찾을 수 없습니다.");
                System.out.println("   💡 Performance 로그가 활성화되어 있는지 확인하세요.");
            } else {
                System.out.println("\n   ✅ 총 " + apiCallCount + "개의 API 관련 로그 발견");
            }

        } catch (Exception e) {
            System.out.println("   ❌ 네트워크 로그 수집 실패: " + e.getMessage());
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

            for (LogEntry entry : logs) {
                String level = entry.getLevel().getName();
                String emoji = getLogEmoji(entry.getLevel());

                System.out.println("\n   " + emoji + " [" + level + "]");
                System.out.println("   시간: " + new java.util.Date(entry.getTimestamp()));
                System.out.println("   메시지: " + entry.getMessage());
            }

        } catch (Exception e) {
            System.out.println("   ❌ 브라우저 로그 수집 실패: " + e.getMessage());
        }
    }

    /**
     * JSON 문자열에서 특정 키의 값 추출
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return "N/A";

            startIndex += searchKey.length();

            // 값이 문자열인 경우
            if (json.charAt(startIndex) == '"') {
                startIndex++;
                int endIndex = json.indexOf("\"", startIndex);
                return json.substring(startIndex, endIndex);
            }

            // 값이 숫자인 경우
            int endIndex = startIndex;
            while (endIndex < json.length() &&
                   (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '.')) {
                endIndex++;
            }
            return json.substring(startIndex, endIndex);

        } catch (Exception e) {
            return "파싱 실패";
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
}
