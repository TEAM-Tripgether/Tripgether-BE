package com.tripgether.web.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Selenium WebDriver 설정
 * 환경별로 로컬 ChromeDriver 또는 Selenium Grid를 사용합니다.
 */
@Slf4j
@Configuration
public class SeleniumConfig {

  @Value("${selenium.grid-url:}")
  private String seleniumGridUrl;

  @Value("${selenium.use-local:true}")
  private boolean useLocal;

  /**
   * Chrome 옵션 설정
   * 헤드리스 모드 및 안정성을 위한 옵션 추가
   */
  @Bean
  public ChromeOptions chromeOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--disable-blink-features=AutomationControlled");
    options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

    return options;
  }

  /**
   * WebDriver 빈 생성
   * selenium.use-local 설정에 따라 로컬 ChromeDriver 또는 Selenium Grid 사용
   *
   * - selenium.use-local=true (기본값): 로컬 ChromeDriver 사용 (개발 환경)
   * - selenium.use-local=false: Selenium Grid 사용 (dev/prod 서버)
   *
   * Lazy 초기화를 통해 실제 API 호출 시점에만 생성됩니다.
   */
  @Bean
  @Scope("prototype")
  @Lazy
  public WebDriver webDriver(ChromeOptions options) {
    if (useLocal) {
      log.info("로컬 ChromeDriver 사용 설정");
      WebDriverManager.chromedriver().setup();
      ChromeDriver driver = new ChromeDriver(options);
      log.info("로컬 ChromeDriver 초기화 완료");
      return driver;
    } else {
      log.info("Selenium Grid 사용 설정: {}", seleniumGridUrl);
      try {
        if (seleniumGridUrl == null || seleniumGridUrl.isEmpty()) {
          throw new IllegalStateException(
              "selenium.use-local=false인데 selenium.grid-url이 설정되지 않았습니다.");
        }

        RemoteWebDriver driver = new RemoteWebDriver(new URL(seleniumGridUrl), options);
        log.info("Selenium Grid에 연결된 WebDriver 초기화 완료: {}", seleniumGridUrl);
        return driver;
      } catch (MalformedURLException e) {
        throw new RuntimeException("Selenium Grid URL이 잘못되었습니다: " + seleniumGridUrl, e);
      }
    }
  }
}
