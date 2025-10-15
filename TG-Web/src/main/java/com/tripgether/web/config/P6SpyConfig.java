package com.tripgether.web.config;

import com.p6spy.engine.spy.P6SpyOptions;
import com.tripgether.global.util.CustomP6SpyFormatter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class P6SpyConfig {

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(CustomP6SpyFormatter.class.getName());
        log.info("🔍 P6Spy SQL 로깅이 활성화되었습니다.");
    }
}
