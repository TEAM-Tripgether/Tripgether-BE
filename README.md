# Tripgether-BE

<!-- 수정하지마세요 자동으로 동기화 됩니다 -->
## 최신 버전 : v0.0.10 (2025-10-15)
[전체 버전 기록 보기](CHANGELOG.md)
</br>

## 📋 목차
- [프로젝트 개요](#-프로젝트-개요)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [주요 기능](#-주요-기능)
- [개발 환경 설정](#-개발-환경-설정)
- [API 문서](#-api-문서)
- [개발 가이드](#-개발-가이드)
- [로깅 시스템](#-로깅-시스템)
- [에러 처리](#-에러-처리)

## 🚀 프로젝트 개요

Tripgether는 여행 동행을 위한 백엔드 API 서버입니다
Clean Architecture와 DDD(Domain-Driven Design) 기반으로 설계되어 확장 가능하고 유지보수가 용이한 구조를 가지고 있습니다.

## 🛠 기술 스택

### Core
- **Java 21** - 최신 LTS 버전
- **Spring Boot 3.5.6** - 메인 프레임워크
- **Spring Data JPA** - 데이터 액세스 계층
- **Spring AOP** - 횡단 관심사 처리

### Database
- **PostgreSQL** - 운영 데이터베이스

### Documentation & Monitoring
- **Swagger/OpenAPI 3** - API 문서화
- **P6Spy** - SQL 로깅 및 모니터링
- **AOP Logging** - 메소드 실행 추적

### Build & Deployment
- **Gradle** - 빌드 도구
- **Docker** - 컨테이너화

## 📂 프로젝트 구조

멀티모듈 Gradle 프로젝트로 구성되어 있습니다.

- **TG-Common**: 공통 라이브러리 (예외 처리, 유틸리티)
- **TG-Member**: 회원 도메인 모듈
- **TG-Application**: 도메인 통합 모듈
- **TG-Web**: 웹 애플리케이션 모듈 (실행 가능한 메인 모듈)

## ✨ 주요 기능

### 1. 공통 응답 시스템
- 성공/실패에 대한 일관된 응답 형식
- 표준화된 에러 코드 및 메시지
- Factory 패턴을 통한 응답 생성

### 2. AOP 로깅 시스템
- Controller → Service → Repository 흐름 추적
- 메소드 실행 시간 측정
- 시각적 계층 구조 표시
- 커스텀 어노테이션 지원

### 3. SQL 로깅 (P6Spy)
- 실행되는 모든 SQL 쿼리 추적
- 바인딩 파라미터 표시
- 실행 시간 측정
- 예쁜 포맷팅

### 4. 소프트 삭제 시스템
- BaseEntity를 통한 공통 감사 추적
- 물리적 삭제 대신 논리적 삭제
- 생성자/수정자/삭제자 추적

### 5. 예외 처리 시스템
- 전역 예외 처리
- 표준화된 에러 응답
- 비즈니스 예외와 시스템 예외 구분

## 🔧 개발 환경 설정

### 1. 필수 요구사항
- Java 21 이상
- PostgreSQL 13 이상 (운영용)
- IDE (IntelliJ IDEA 권장)

## 👨‍💻 개발 가이드

### 1. 에러 코드 관리 전략

#### 1.1 ErrorCodeContainer 사용 (권장)

복잡한 비즈니스 로직이나 동적으로 에러 메시지를 생성해야 하는 경우:

```java
/**
 * 동적 에러 코드 생성 예시
 * 비즈니스 로직에 따라 다양한 에러 상황을 처리할 때 사용
 */
@Service
public class UserService {
    
    /**
     * 사용자 권한 검증 후 에러 코드 동적 생성
     */
    public void validateUserPermission(Long userId, String action) {
        // 복잡한 비즈니스 검증 로직...
        
        if (someComplexCondition) {
            // 에러 코드 생성 및 예외 처리 예시
            // ErrorCodeFactory를 사용하여 에러 코드 생성 후 ErrorCodeContainer로 변환
            // CustomException를 통해 예외 발생
            ErrorCodeContainer errorCode = ErrorCodeFactory
                    .fail(Subject.EXAMPLE, Action.FIND, HttpStatus.NOT_FOUND);
            
            throw new CustomException(errorCode);
            
        }
    }
}
```

#### 1.2 ErrorCode 직접 사용

간단하고 자주 사용되는 에러의 경우:

```java
// ErrorCode.java에 추가
/**
 * 여행 관련 에러 코드
 */
TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TRIP404", "요청한 여행을 찾을 수 없습니다."),
TRIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "TRIP409", "이미 존재하는 여행입니다."),
TRIP_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "TRIP403", "여행 수정 권한이 없습니다."),

/**
 * 사용 예시
 */
@Service
public class TripService {
    public TripDto getTrip(Long id) {
        return tripRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));
    }
}
```

### 3. Java Doc 작성 가이드

#### 3.1 클래스 레벨 Java Doc

```java
/**
 * 여행 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>여행 생성/수정/삭제</li>
 *   <li>여행 검색 및 필터링</li>
 *   <li>여행 참가자 관리</li>
 * </ul>
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * TripDto trip = tripService.createTrip(createRequest);
 * List<TripDto> trips = tripService.searchTrips("제주도");
 * }</pre>
 * 
 * @author 개발자명
 * @since 1.0.0
 * @version 1.2.0
 * @see TripEntity
 * @see TripRepository
 */
@Service
public class TripService {
    // 구현...
}
```

#### 3.2 메소드 레벨 Java Doc

```java
/**
 * 여행 목록을 페이징하여 조회합니다.
 * 
 * <p>검색 조건에 따라 필터링된 여행 목록을 반환하며,
 * 삭제된 여행은 제외됩니다.</p>
 * 
 * @param searchCondition 검색 조건 (null 가능)
 * @param pageable 페이징 정보 (필수)
 * @return 페이징된 여행 목록
 * @throws IllegalArgumentException pageable이 null인 경우
 * @throws CustomException 데이터베이스 조회 실패 시
 * 
 * @since 1.0.0
 * @author 개발자명
 * 
 * @apiNote 이 메소드는 읽기 전용 트랜잭션에서 실행됩니다.
 * @implNote 소프트 삭제된 여행은 자동으로 제외됩니다.
 * 
 * @see TripSearchCondition
 * @see org.springframework.data.domain.Pageable
 */
@Transactional(readOnly = true)
public Page<TripDto> searchTrips(TripSearchCondition searchCondition, Pageable pageable) {
    if (pageable == null) {
        throw new IllegalArgumentException("Pageable은 필수입니다.");
    }
    
    // 구현...
}
```

#### 3.3 필드 레벨 Java Doc

```java
public class TripEntity extends BaseEntity {
    
    /**
     * 여행 고유 식별자
     * 
     * <p>자동 증가하는 기본키로 사용됩니다.</p>
     * 
     * @since 1.0.0
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 여행 제목
     * 
     * <p>제약사항:</p>
     * <ul>
     *   <li>필수 입력 항목</li>
     *   <li>최대 100자까지 입력 가능</li>
     *   <li>중복 불가</li>
     * </ul>
     * 
     * @since 1.0.0
     */
    @Column(nullable = false, length = 100, unique = true)
    private String title;
}
```

### 커스텀 로깅 사용

```java
/**
 * 복잡한 비즈니스 로직 수행
 * 
 * @param id 처리할 데이터 ID
 * @return 처리 결과
 */
@LogExecutionTime(description = "복잡한 비즈니스 로직", threshold = 1000)
public YourDto complexOperation(Long id) {
    // 시간이 오래 걸리는 작업...
    return result;
}
```

### 4. 개발 시 주의사항

#### 4.1 에러 처리 우선순위
1. **ErrorCodeContainer** 사용 (복잡한 로직, 동적 메시지)
2. **ErrorCode** 직접 사용 (간단한 케이스, 자주 사용되는 에러)

#### 4.2 Java Doc 필수 작성 대상
- 모든 public 클래스
- 모든 public 메소드
- 복잡한 비즈니스 로직을 포함한 private 메소드
- 중요한 필드 (설정값, 비즈니스 규칙 관련)

#### 4.3 코드 품질 체크리스트
- [ ] Java Doc 작성 완료
- [ ] 적절한 에러 처리 (ErrorCodeContainer vs ErrorCode)
- [ ] 로깅 어노테이션 적용 (@LogExecutionTime)
- [ ] Swagger 어노테이션 작성
- [ ] 유닛 테스트 작성

## 📊 로깅 시스템

### AOP 메소드 로깅

실행 흐름이 자동으로 로깅됩니다:

```
🚀 🎯 ExampleController.createExample()
  ↳ ⚙️ ExampleService.createExample()
    ↳ 💾 ExampleRepository.save()
    ✅ 💾 ExampleRepository.save() 완료 [15ms]
  ✅ ⚙️ ExampleService.createExample() 완료 [25ms]
✅ 🎯 ExampleController.createExample() 완료 [30ms]
📋 실행 흐름: ExampleController.createExample → ExampleService.createExample → ExampleRepository.save
════════════════════════════════════════════════════════════════════════════════
```

### SQL 로깅 (P6Spy)

```
[SQL] 15ms | insert into examples (name, description) values (?, ?)
```

### 로깅 레벨 설정

```yaml
# application-dev.yml
logging:
  level:
    com.tripgether.be: INFO    # 애플리케이션 로그
    p6spy: INFO               # SQL 로그
    org.springframework: WARN  # Spring 로그 최소화
```

## ❌ 에러 처리

### 표준 에러 응답 형식

```json
{
  "code": "EXAMPLE_FIND_404",
  "message": "예시 조회에 실패했습니다."
}
```

### 비즈니스 예외 발생

```java
// Service에서
if (!repository.existsById(id)) {
    throw new BusinessException(ErrorCode.EXAMPLE_NOT_FOUND);
}
```

### 성공 응답 형식

```json
{
  "code": "EXAMPLE_FIND_200",
  "message": "예시이(가) 성공적으로 조회되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "샘플 예시"
    },
    {
      "id": 2,
      "name": "샘플 예시"
    }
  ]
}
```

## 🤝 기여 가이드

1. 새로운 기능 개발 시 `domain` 패키지 구조를 따라주세요
2. 모든 API는 공통 응답 형식을 사용해주세요
3. 비즈니스 예외는 `BusinessException`을 사용해주세요
4. 중요한 메소드에는 `@LogExecutionTime` 어노테이션을 추가해주세요
5. API 문서화를 위해 Swagger 어노테이션을 활용해주세요

## 📞 문의

프로젝트 관련 문의사항이 있으시면 팀 리드에게 연락주세요.
