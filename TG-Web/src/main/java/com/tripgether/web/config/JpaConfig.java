package com.tripgether.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** JPA 설정 BaseEntity의 @CreatedDate, @LastModifiedDate를 위한 JPA Auditing 활성화 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
