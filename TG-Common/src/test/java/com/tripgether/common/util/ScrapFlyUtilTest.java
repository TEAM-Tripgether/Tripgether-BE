package com.tripgether.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.lineLog;
import static me.suhsaechan.suhlogger.util.SuhLogger.timeLog;

import com.tripgether.web.TripgetherApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = TripgetherApplication.class)
@ActiveProfiles("dev")
@Slf4j
class ScrapFlyUtilTest {

  @Autowired
  ScrapFlyUtil scrapFlyUtil;

  @Test
  public void mainTest() {
    lineLog("테스트시작");

    lineLog(null);
    timeLog(this::test);
    lineLog(null);

    lineLog("테스트종료");
  }

  public void test(){
  }

}