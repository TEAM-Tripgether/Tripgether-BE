# ===================================================================
# Spring Boot Dockerfile
# ===================================================================
# 
# 📝 설명:
# - Spring Boot 애플리케이션을 위한 멀티 스테이지 Dockerfile
# - 빌드 결과물(app.jar)을 경량화된 런타임 이미지로 실행
# 
# 🔧 빌드 구조:
# - Gradle 빌드 시 build/libs/app.jar 생성 가정
# - Java 17 기반 런타임 환경
# 
# ===================================================================

# ===================================================================
# 런타임 스테이지
# ===================================================================
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
# Gradle 빌드 시 build/libs/app.jar 파일이 생성된다고 가정
COPY build/libs/app.jar app.jar

# 타임존 설정 (Asia/Seoul)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 포트 노출 (Spring Boot 기본 포트)
EXPOSE 8080

# 애플리케이션 실행
# JVM 옵션:
# - Xms512m: 초기 힙 메모리 512MB
# - Xmx1024m: 최대 힙 메모리 1GB
# - 필요에 따라 조정 가능
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]

# ===================================================================
# 사용 방법
# ===================================================================
#
# 1. Gradle 빌드:
#    ./gradlew clean build -x test
#
# 2. Docker 이미지 빌드:
#    docker build -t your-project-name:latest .
#
# 3. Docker 컨테이너 실행:
#    docker run -d -p 8080:8080 \
#      -e SPRING_PROFILES_ACTIVE=prod \
#      your-project-name:latest
#
# ===================================================================
