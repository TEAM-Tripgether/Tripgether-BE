# CLAUDE.md - Tripgether Backend

이 파일은 Claude Code가 이 프로젝트를 이해하고 작업할 때 참고하는 가이드입니다.

## 프로젝트 개요

Tripgether는 여행 동행을 위한 백엔드 API 서버입니다.
Clean Architecture와 DDD(Domain-Driven Design) 기반으로 설계되었습니다.

## 기술 스택

- **Java 21** (LTS)
- **Spring Boot 3.5.6**
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL**
- **Flyway** (DB 마이그레이션)
- **Gradle** (빌드 도구)
- **Lombok**

## 프로젝트 구조 (멀티 모듈)

```
Tripgether-BE/
├── TG-Web/          # 메인 애플리케이션 (실행 모듈)
├── TG-Auth/         # 인증/인가 모듈
├── TG-Member/       # 회원 도메인
├── TG-Place/        # 장소 도메인
├── TG-SNS/          # SNS 기능
├── TG-AI/           # AI 연동
├── TG-Application/  # 애플리케이션 서비스
└── TG-Common/       # 공통 유틸리티, 예외 처리
```

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew :TG-Web:bootRun

# 테스트
./gradlew test
```

## 코드 스타일 컨벤션

### Java 클래스

- **Lombok 사용**: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **생성자 주입**: `@RequiredArgsConstructor` 사용 (필드 주입 X)
- **컨트롤러 반환**: `ResponseEntity<T>` 사용
- **DTO 네이밍**: `XxxRequest`, `XxxResponse`

### 패키지 구조

레이어별 구조:
```
com.tripgether.{module}/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
└── constant/
```

### 엔티티

- `BaseEntity` 또는 `SoftDeletableBaseEntity` 상속
- 소프트 삭제 사용 (`deleted_at` 컬럼)

## Flyway 마이그레이션 규칙

**중요**: DB를 전체 삭제 후 서버 시작하는 경우가 있으므로, 모든 마이그레이션은 **테이블 존재 여부를 먼저 확인**해야 합니다.

### 파일 네이밍

```
V{version}__{description}.sql
```

예: `V0.2.36__cleanup_orphan_fcm_tokens.sql`

### 필수 패턴

```sql
DO
$$
    BEGIN
        -- 테이블 존재 확인 후 작업
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = '테이블명') THEN
            -- 작업 수행
        ELSE
            RAISE NOTICE 'Table does not exist. Skipping migration.';
        END IF;
    END
$$;
```

### FK 제약조건 작성 시

부모 테이블 존재 여부를 **반드시** 먼저 확인:

```sql
DO $$
BEGIN
    -- 1. 부모 테이블 존재 확인
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'member') THEN
        -- 2. 자식 테이블 작업
    ELSE
        RAISE NOTICE 'Parent table does not exist. JPA will create the table.';
    END IF;
END $$;
```

자세한 내용은 `TG-Web/src/main/resources/db/migration/README.md` 참고.

## 주의사항

### JPA DDL 전략

- `spring.jpa.hibernate.ddl-auto=update` 사용
- 실행 순서: Flyway 마이그레이션 → JPA DDL
- 테이블이 없으면 Flyway는 스킵하고 JPA가 생성

### 소프트 삭제

- Member, MemberInterest 등은 소프트 삭제 사용
- FK의 `ON DELETE CASCADE`는 하드삭제에만 작동
- 연관 엔티티 삭제 시 명시적으로 처리 필요

### 회원 탈퇴 시

`AuthService.withdrawMember()`에서 처리:
- Member: 소프트삭제
- MemberInterest: 소프트삭제
- FcmToken: 하드삭제 (FK 제약조건 위반 방지)

## 자주 사용하는 경로

- 메인 설정: `TG-Web/src/main/resources/application.yml`
- 마이그레이션: `TG-Web/src/main/resources/db/migration/`
- 예외 처리: `TG-Common/src/main/java/com/tripgether/common/exception/`
- 인증: `TG-Auth/src/main/java/com/tripgether/auth/`
