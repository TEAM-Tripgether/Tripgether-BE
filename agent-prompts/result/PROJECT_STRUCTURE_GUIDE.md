# Tripgether 프로젝트 구조 가이드

## 📂 프로젝트 구조

```
Tripgether-BE/
├── TG-Common/          # 공통 라이브러리 모듈
├── TG-Member/          # 회원 도메인 모듈
├── TG-Application/     # 도메인 통합 모듈
├── TG-Web/             # 웹 애플리케이션 모듈 (실행 가능)
└── build.gradle        # 루트 빌드 설정
```

## 🎯 모듈별 역할

### TG-Common (공통 라이브러리)
- **역할**: 모든 모듈에서 사용하는 공통 기능 제공
- **포함 기능**:
  - 예외 처리 (`CustomException`, `GlobalExceptionHandler`)
  - 공통 엔티티 (`BaseEntity`, `SoftDeletableBaseEntity`)
  - 유틸리티 (`TimeUtil`, `CustomP6SpyFormatter`)
  - 로깅 AOP (`MethodLoggingAspect`)
  - 상수 정의 (`ErrorCode`, `ErrorMessageTemplate`)
- **의존성**: Spring Boot 기본 의존성 + PostgreSQL JDBC 드라이버

### TG-Member (회원 도메인)
- **역할**: 회원 관련 비즈니스 로직 처리
- **포함 기능**:
  - Entity (`MemberEntity`)
  - Repository (`MemberRepository`)
  - Service (`MemberService`)
  - DTO (`MemberDto`)
- **의존성**: TG-Common만 의존

### TG-Application (도메인 통합)
- **역할**: 여러 도메인에 걸친 복잡한 비즈니스 로직 처리
- **포함 기능**:
  - 도메인 간 조합 로직
  - 복잡한 트랜잭션 처리
- **의존성**: TG-Common + 모든 도메인 모듈

### TG-Web (웹 애플리케이션)
- **역할**: 실행 가능한 메인 모듈
- **포함 기능**:
  - Controller (`MemberController`, 기타 컨트롤러)
  - Configuration (`ComponentScanConfig`, `SwaggerConfig`, `JpaConfig`)
  - Application (`TripgetherApplication`)
- **의존성**: TG-Common + TG-Member + TG-Application

## 📦 패키지 배치 규칙

### Controller 위치
```
TG-Web/src/main/java/com/tripgether/web/controller/
```
- **역할**: REST API 엔드포인트 제공
- **규칙**: 모든 Controller는 `TG-Web` 모듈에 위치
- **예시**: `UserController`, `CourseController`, `AuthController`

### Config 위치
```
TG-Web/src/main/java/com/tripgether/web/config/
```
- **역할**: Spring 설정 클래스
- **규칙**: 모든 Configuration은 `TG-Web` 모듈에 위치
- **예시**: `SecurityConfig`, `DatabaseConfig`, `CacheConfig`

### Util 위치
```
TG-Common/src/main/java/com/tripgether/global/util/
```
- **역할**: 공통 유틸리티 함수
- **규칙**: 모든 Util은 `TG-Common` 모듈에 위치
- **예시**: `DateUtil`, `StringUtil`, `ValidationUtil`

### Constant 위치
```
TG-Common/src/main/java/com/tripgether/global/constant/
```
- **역할**: 상수 정의
- **규칙**: 모든 Constant는 `TG-Common` 모듈에 위치
- **예시**: `ApiConstants`, `DatabaseConstants`, `SecurityConstants`

## 🔗 의존성 추가 방법

### 공통 의존성 (TG-Common)
```gradle
// TG-Common/build.gradle
dependencies {
    // Spring Boot 기본 의존성
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // 외부 라이브러리
    api 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3'
    api 'org.postgresql:postgresql:42.7.7'
}
```

### 도메인별 의존성 (TG-Member, TG-Post 등)
```gradle
// TG-Member/build.gradle
dependencies {
    // TG-Common 의존
    api project(':TG-Common')
    
    // 도메인별 특수 의존성 (필요시)
    // api 'specific-library:version'
}
```

### 웹 애플리케이션 의존성 (TG-Web)
```gradle
// TG-Web/build.gradle
dependencies {
    // 모든 모듈 의존
    implementation project(':TG-Common')
    implementation project(':TG-Member')
    implementation project(':TG-Application')
    implementation project(':TG-Post')      // 새 도메인 추가시
}
```

## 🆕 새로운 도메인 모듈 추가 방법

### 1. 모듈 생성
```bash
# 새 도메인 모듈 디렉토리 생성
mkdir TG-User
```

### 2. build.gradle 설정
```gradle
// TG-User/build.gradle
plugins {
    id 'java-library'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

bootJar {
    enabled = false
}

jar {
    enabled = true
    archiveClassifier = ''
}

dependencies {
    api project(':TG-Common')
}
```

### 3. 패키지 구조
```
TG-User/src/main/java/com/tripgether/domain/user/
├── entity/
│   └── UserEntity.java
├── repository/
│   └── UserRepository.java
├── service/
│   └── UserService.java
└── dto/
    ├── request/
    │   └── UserCreateRequest.java
    └── response/
        └── UserResponse.java
```

### 4. settings.gradle에 모듈 추가
```gradle
// settings.gradle
include 'TG-Common'
include 'TG-Member'
include 'TG-Application'
include 'TG-Web'
include 'TG-Post'        // 새 모듈 추가 예시
```

### 5. TG-Web에 의존성 추가
```gradle
// TG-Web/build.gradle
dependencies {
    implementation project(':TG-Common')
    implementation project(':TG-Member')
    implementation project(':TG-Application')
    implementation project(':TG-Post')      // 새 모듈 의존성 추가 예시
}
```

### 6. ComponentScan 설정 업데이트
```java
// TG-Web/src/main/java/com/tripgether/web/config/ComponentScanConfig.java
@ComponentScan(basePackages = {
    "com.tripgether.common",
    "com.tripgether.domain.member",
    "com.tripgether.domain.post",    // 새 도메인 패키지 추가 예시
    "com.tripgether.web"
})
```

## ⚠️ 주의사항

### 의존성 방향
- **TG-Common** → 다른 모듈 의존 금지
- **TG-Member, TG-Post** 등 도메인 모듈 → TG-Common만 의존
- **TG-Application** → TG-Common + 모든 도메인 모듈 의존
- **TG-Web** → 모든 모듈 의존 가능

### 패키지 네이밍 규칙
- **공통 기능**: `com.tripgether.common.*`
- **도메인 기능**: `com.tripgether.domain.{domain}.*`
- **웹 기능**: `com.tripgether.web.*`

### 빌드 설정
- **공통 모듈**: `bootJar { enabled = false }`
- **웹 모듈**: `bootJar { enabled = true }`

## 🚀 빠른 시작

1. **새 도메인 모듈 생성**: 위의 "새로운 도메인 모듈 추가 방법" 참고
2. **패키지 구조 생성**: entity, repository, service, dto 패키지 생성
3. **의존성 설정**: TG-Common 의존성 추가
4. **TG-Web 설정**: 새 모듈 의존성 및 ComponentScan 추가
5. **개발 시작**: 기존 TG-Example 패턴 참고하여 개발

---

**작성일**: 2025-01-15  
**버전**: v1.0.0
