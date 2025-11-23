# Tripgether-BE

<!-- 수정하지마세요 자동으로 동기화 됩니다 -->
## 최신 버전 : v0.2.25 (2025-11-22)
[전체 버전 기록 보기](CHANGELOG.md)
</br>

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