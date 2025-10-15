# Tripgether 모듈 추가 가이드

## 📋 개요

이 문서는 Tripgether 프로젝트에 새로운 도메인 모듈을 추가하는 방법을 단계별로 설명합니다. 현재 프로젝트는 멀티모듈 구조로 구성되어 있으며, 새 모듈 추가 시 일관된 구조와 의존성을 유지해야 합니다.

**프로젝트 구조**: Spring Boot 멀티모듈 (Gradle)  
**현재 모듈**: TG-Common, TG-Example, TG-Application, TG-Web  
**문서 버전**: v1.0.0

---

## 🎯 모듈 추가 시 변경사항 요약

### 필수 변경사항 (3개 파일)
1. **settings.gradle** - 새 모듈 등록
2. **TG-Web/build.gradle** - Web 모듈에 의존성 추가
3. **TG-Application/build.gradle** - Application 모듈에 의존성 추가

### 자동 처리되는 설정
- **ComponentScanConfig.java** - 와일드카드 패턴으로 자동 스캔
- **application.yml** - 패키지 스캔 자동 적용
- **로깅 시스템** - AOP 기반 자동 로깅 적용

---

## 📝 단계별 모듈 추가 가이드

### Step 1: 새 모듈 디렉토리 생성

```bash
# 프로젝트 루트에서 실행
mkdir TG-Member  # 예시: 사용자 모듈
```

### Step 2: 모듈 기본 구조 생성

```bash
# 패키지 구조 생성
mkdir -p TG-Member/src/main/java/com/tripgether/domain/member/{entity,repository,service,dto}
mkdir -p TG-Member/src/main/resources
```

### Step 3: build.gradle 파일 생성

**파일 위치**: `TG-Member/build.gradle`

```gradle
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
    // TG-Common 의존
    api project(':TG-Common')
}
```

### Step 4: 패키지 구조 및 .gitkeep 파일 생성

```bash
# 각 패키지에 .gitkeep 파일 생성
touch TG-Member/src/main/java/com/tripgether/domain/member/.gitkeep
touch TG-Member/src/main/java/com/tripgether/domain/member/entity/.gitkeep
touch TG-Member/src/main/java/com/tripgether/domain/member/repository/.gitkeep
touch TG-Member/src/main/java/com/tripgether/domain/member/service/.gitkeep
touch TG-Member/src/main/java/com/tripgether/domain/member/dto/.gitkeep
```

**.gitkeep 파일 내용**:
```markdown
# 이 파일은 빈 패키지를 Git에 포함시키기 위한 파일입니다.
# 실제 클래스들이 추가되면 이 파일은 삭제해도 됩니다.
```

### Step 5: settings.gradle에 모듈 추가

**파일 위치**: `settings.gradle`

```gradle
rootProject.name = 'tripgether'

include 'TG-Common'
include 'TG-Example'
include 'TG-Application'
include 'TG-Member'        // 새 모듈 추가
include 'TG-Web'
```

### Step 6: TG-Web/build.gradle에 의존성 추가

**파일 위치**: `TG-Web/build.gradle`

```gradle
dependencies {
    // 모든 모듈 의존
    implementation project(':TG-Common')
    implementation project(':TG-Example')
    implementation project(':TG-Application')
    implementation project(':TG-Member')     // 새 모듈 의존성 추가
}
```

### Step 7: TG-Application/build.gradle에 의존성 추가

**파일 위치**: `TG-Application/build.gradle`

```gradle
dependencies {
    // 모든 도메인 모듈 의존
    api project(':TG-Common')
    api project(':TG-Example')
    api project(':TG-Member')               // 새 모듈 의존성 추가
    // 향후 추가될 도메인들
    // api project(':TG-Post')
    // api project(':TG-Place')
}
```

### Step 8: 빌드 테스트

```bash
# 프로젝트 루트에서 실행
./gradlew build --no-daemon
```

---

## 📦 모듈별 역할 및 책임

### 도메인 모듈 (TG-Member, TG-Post 등)
- **역할**: 특정 도메인의 비즈니스 로직 처리
- **포함 기능**:
  - Entity (도메인 엔티티)
  - Repository (데이터 접근 계층)
  - Service (비즈니스 로직)
  - DTO (데이터 전송 객체)
- **의존성**: TG-Common만 의존

### TG-Application 모듈
- **역할**: 여러 도메인 간 통합 비즈니스 로직 처리
- **포함 기능**:
  - Application Service (복잡한 워크플로우 오케스트레이션)
  - 도메인 간 의존성 조합
  - 트랜잭션 경계 관리
- **의존성**: 모든 도메인 모듈 의존

### TG-Web 모듈
- **역할**: 웹 계층 및 실행 가능한 메인 모듈
- **포함 기능**:
  - Controller (REST API 엔드포인트)
  - Configuration (Spring 설정)
  - Application (메인 애플리케이션 클래스)
- **의존성**: 모든 모듈 의존

---

## 🔗 의존성 구조

```
TG-Web → TG-Application → TG-Member + TG-Example + TG-Common
```

### 의존성 방향 규칙
- **TG-Common** → 다른 모듈 의존 금지
- **도메인 모듈** → TG-Common만 의존
- **TG-Application** → 모든 도메인 모듈 의존
- **TG-Web** → 모든 모듈 의존 가능

---

## 📋 패키지 네이밍 규칙

### 도메인 모듈 패키지 구조
```
com.tripgether.domain.{domain}/
├── entity/           # 도메인 엔티티
├── repository/       # 데이터 접근 계층
├── service/          # 비즈니스 로직
└── dto/              # 데이터 전송 객체
    ├── request/      # 요청 DTO
    └── response/     # 응답 DTO
```

### 클래스 네이밍 컨벤션
- **Entity**: `{Domain}Entity` (예: `UserEntity`, `CourseEntity`)
- **Repository**: `{Domain}Repository` (예: `UserRepository`, `CourseRepository`)
- **Service**: `{Domain}Service` (예: `UserService`, `CourseService`)
- **DTO**: `{Domain}Dto` (예: `UserDto`, `CourseDto`)
- **Controller**: `{Domain}Controller` (예: `UserController`, `CourseController`)

---

## ⚙️ 자동 처리되는 설정

### ComponentScan 자동 적용
**파일**: `TG-Web/src/main/java/com/tripgether/web/config/ComponentScanConfig.java`

```java
@ComponentScan(basePackages = "com.tripgether")
```

- 와일드카드 패턴으로 `com.tripgether` 하위 모든 패키지 자동 스캔
- 새 모듈 추가 시 별도 설정 불필요

### Application.yml 자동 스캔
**파일**: `TG-Web/src/main/resources/application.yml`

```yaml
springdoc:
  packages-to-scan: com.tripgether
```

- Swagger/OpenAPI 문서 자동 생성
- 새 모듈의 Controller 자동 포함

### 로깅 시스템 자동 적용
**파일**: `TG-Common/src/main/java/com/tripgether/global/logging/MethodLoggingAspect.java`

```java
@Around("within(com.tripgether..*) && " +
        "(execution(* *Controller.*(..)) || " +
        " execution(* *Service.*(..)) || " +
        " execution(* *Repository.*(..)))")
```

- AOP 기반 자동 로깅
- 실행 시간 측정 및 호출 흐름 추적
- 새 모듈의 Controller, Service, Repository 자동 적용

---

## 🚀 실제 모듈 추가 예시

### TG-Member 모듈 추가 예시

#### 1. 디렉토리 구조 생성
```bash
mkdir -p TG-Member/src/main/java/com/tripgether/domain/member/{entity,repository,service,dto}
```

#### 2. build.gradle 생성
```gradle
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

#### 3. 설정 파일 업데이트
```gradle
# settings.gradle
include 'TG-Member'

# TG-Web/build.gradle
implementation project(':TG-Member')

# TG-Application/build.gradle
api project(':TG-Member')
```

#### 4. 기본 클래스 생성 예시
```java
// TG-Member/src/main/java/com/tripgether/domain/member/entity/MemberEntity.java
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String nickname;
    
    // ... 기타 필드
}

// TG-Member/src/main/java/com/tripgether/domain/member/repository/MemberRepository.java
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}

// TG-Member/src/main/java/com/tripgether/domain/member/service/MemberService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    
    public MemberDto createMember(MemberDto memberDto) {
        // 비즈니스 로직 구현
    }
    
    public MemberDto getMember(Long id) {
        // 조회 로직 구현
    }
}
```

---

## ⚠️ 주의사항 및 베스트 프랙티스

### 의존성 관리
- **순환 의존성 방지**: 도메인 모듈 간 직접 의존성 금지
- **의존성 방향 준수**: TG-Common → 도메인 모듈 → TG-Application → TG-Web
- **공통 기능은 TG-Common에**: 여러 모듈에서 사용하는 기능은 TG-Common에 배치

### 패키지 구조
- **일관성 유지**: 기존 모듈과 동일한 패키지 구조 사용
- **명확한 네이밍**: 도메인명은 명확하고 간결하게
- **계층 분리**: Entity, Repository, Service, DTO 계층 명확히 분리

### 빌드 설정
- **bootJar 비활성화**: 도메인 모듈은 `bootJar { enabled = false }`
- **jar 활성화**: `jar { enabled = true }`
- **의존성 범위**: 도메인 모듈은 `api` 사용, Web 모듈은 `implementation` 사용

### 테스트
- **단위 테스트**: 각 모듈별 단위 테스트 작성
- **통합 테스트**: Web 모듈에서 통합 테스트 수행
- **테스트 데이터**: 테스트용 데이터 및 Mock 객체 활용

---

## 🔍 문제 해결 가이드

### 빌드 실패 시
1. **의존성 확인**: settings.gradle에 모듈이 등록되었는지 확인
2. **의존성 방향 확인**: 순환 의존성이 없는지 확인
3. **패키지 경로 확인**: 패키지명이 올바른지 확인

### 런타임 에러 시
1. **ComponentScan 확인**: 와일드카드 패턴이 올바른지 확인
2. **Bean 등록 확인**: @Service, @Repository 어노테이션 확인
3. **의존성 주입 확인**: @RequiredArgsConstructor 사용 확인

### 로깅 문제 시
1. **AOP 설정 확인**: MethodLoggingAspect가 올바르게 설정되었는지 확인
2. **패키지 경로 확인**: `com.tripgether..*` 패턴에 포함되는지 확인
3. **클래스명 확인**: *Controller, *Service, *Repository 패턴 확인

---

## 📚 추가 자료

### 관련 문서
- [PROJECT_STRUCTURE_GUIDE.md](./PROJECT_STRUCTURE_GUIDE.md) - 프로젝트 전체 구조 가이드
- [PROJECT_SPECIFICATION.md](./PROJECT_SPECIFICATION.md) - 프로젝트 명세서
- [ERD_DESIGN.md](./ERD_DESIGN.md) - 데이터베이스 설계

### 참고 모듈
- **TG-Example**: 예시 도메인 모듈 (참고용)
- **TG-Common**: 공통 라이브러리 모듈
- **TG-Application**: 통합 비즈니스 로직 모듈

### 개발 도구
- **IntelliJ IDEA**: 권장 IDE
- **Gradle**: 빌드 도구
- **Spring Boot**: 프레임워크
- **PostgreSQL**: 데이터베이스

---

## 🎯 체크리스트

### 모듈 추가 전
- [ ] 모듈명 및 도메인명 확정
- [ ] 패키지 구조 설계
- [ ] 의존성 관계 분석

### 모듈 추가 중
- [ ] 디렉토리 구조 생성
- [ ] build.gradle 파일 생성
- [ ] .gitkeep 파일 생성
- [ ] settings.gradle 업데이트
- [ ] 의존성 설정 업데이트

### 모듈 추가 후
- [ ] 빌드 테스트 통과
- [ ] 기본 클래스 생성
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 수행
- [ ] 문서 업데이트

---

**작성일**: 2025-10-15  
**작성자**: Tripgether 개발팀  
**버전**: v1.0.0  
**다음 업데이트**: 모듈 추가 시 실제 사례 반영
