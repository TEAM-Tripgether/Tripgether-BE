package com.tripgether.web.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 데이터베이스 마이그레이션 설정
 *
 * <p>application.yml의 flyway 설정을 통해 자동으로 마이그레이션이 실행됩니다.</p>
 *
 * <h3>주요 설정</h3>
 * <ul>
 *   <li>baseline-on-migrate: true - 기존 DB에서도 안전하게 마이그레이션</li>
 *   <li>baseline-version: 0.1.0 - 초기 베이스라인 버전</li>
 *   <li>locations: classpath:db/migration - 마이그레이션 스크립트 위치</li>
 * </ul>
 *
 * <h3>마이그레이션 파일 네이밍 규칙</h3>
 * <pre>
 * V{version}__{description}.sql
 * 예: V0.1.5__simplify_media_structure.sql
 * </pre>
 *
 * @see org.flywaydb.core.Flyway
 * @see org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
 */
@Configuration
public class FlywayConfig {

  /**
   * Flyway 마이그레이션 전략 설정
   *
   * <p>마이그레이션 실행 전 정보를 출력하고, 마이그레이션을 수행합니다.</p>
   *
   * @return FlywayMigrationStrategy 마이그레이션 전략
   */
  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      // Flyway 정보 출력
      flyway.info();

      // 마이그레이션 실행
      flyway.migrate();
    };
  }
}

