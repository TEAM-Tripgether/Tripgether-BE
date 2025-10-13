# 🚀 SUH-DEVOPS-TEMPLATE 빠른 시작 가이드

> **3가지만 하면 끝!** 나머지는 모두 자동화됩니다.

이 템플릿은 GitHub 템플릿 또는 원격 스크립트로 설치하면 **자동으로 초기화**됩니다.  
사용자는 단 **3가지 설정**만 하면 모든 자동화가 작동합니다.

---

## 🎯 3가지 필수 작업

### 1️⃣ GitHub Personal Access Token 설정

#### 토큰 생성
1. **GitHub** → **Settings** → **Developer settings** → **Personal access tokens (Classic)**
2. **Generate new token (classic)** 클릭
3. 토큰 설정:
   - **Name**: `_GITHUB_PAT_TOKEN`
   - **Expiration**: 90 days (또는 조직 정책에 따라)
   - **Scopes**: ✅ `repo` (Full control), ✅ `workflow` (Update workflows)
4. **Generate token** 클릭 후 토큰 복사

#### Secret 등록
1. **프로젝트 저장소** → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret** 클릭
3. **Name**: `_GITHUB_PAT_TOKEN`
4. **Secret**: [위에서 복사한 토큰 값 붙여넣기]
5. **Add secret** 클릭

---

### 2️⃣ deploy 브랜치 생성

```bash
# deploy 브랜치 생성 및 푸시
git checkout -b deploy
git push -u origin deploy

# main 브랜치로 돌아가기
git checkout main
```

**설명**: `deploy` 브랜치는 체인지로그 자동 생성 및 배포 워크플로우가 실행되는 브랜치입니다.

---

### 3️⃣ CodeRabbit 활성화

1. [CodeRabbit 웹사이트](https://coderabbit.ai) 접속
2. GitHub 계정으로 로그인
3. 저장소 목록에서 프로젝트 선택하여 활성화
4. `.coderabbit.yaml` 파일이 프로젝트에 있으면 자동으로 설정 적용됨

**설명**: CodeRabbit은 AI 기반 코드 리뷰 및 체인지로그 자동 생성을 담당합니다.

---

## 🎉 완료!

**이제 코드를 푸시하면 모든 자동화가 작동합니다.**

- ✅ 버전 자동 증가 (1.0.0 → 1.0.1)
- ✅ README 버전 자동 업데이트
- ✅ Git 태그 자동 생성
- ✅ 체인지로그 자동 생성

---

## ✨ 자동으로 처리되는 것들

### 템플릿 사용 시 (GitHub "Use this template")

**프로젝트 생성 즉시 자동 실행**:
- ✅ `version.yml` 자동 생성 (v0.0.0, basic 타입)
- ✅ 공통 워크플로우 5개 자동 설치:
  - `PROJECT-COMMON-VERSION-CONTROL.yaml` (버전 자동 관리)
  - `PROJECT-COMMON-AUTO-CHANGELOG-CONTROL.yaml` (체인지로그 생성)
  - `PROJECT-COMMON-README-VERSION-UPDATE.yaml` (README 업데이트)
  - `PROJECT-COMMON-ISSUE-COMMENT.yaml` (이슈 자동화)
  - `PROJECT-COMMON-SYNC-ISSUE-LABELS.yaml` (라벨 동기화)
- ✅ README 버전 섹션 자동 추가
- ✅ 불필요한 템플릿 파일 자동 삭제
- ✅ Default 브랜치 자동 감지

---

### 원격 스크립트 사용 시 (기존 프로젝트에 통합)

```bash
# 대화형 모드 (권장)
bash <(curl -fsSL "https://raw.githubusercontent.com/Cassiiopeia/SUH-DEVOPS-TEMPLATE/main/template_integrator.sh")

# 비대화형 모드 (Spring Boot 전체 통합 예시)
bash <(curl -fsSL "https://raw.githubusercontent.com/Cassiiopeia/SUH-DEVOPS-TEMPLATE/main/template_integrator.sh") \
  --mode full --type spring --version 1.0.0 --force
```

**자동으로 수행되는 작업**:
- ✅ 프로젝트 타입 자동 감지 (Spring, Flutter, React, Node, Python 등)
- ✅ 현재 버전 자동 감지 (Git 태그, build.gradle, package.json 등)
- ✅ 프로젝트 타입에 맞는 워크플로우만 선택 복사
- ✅ `version.yml` 자동 생성
- ✅ README에 버전 섹션 자동 추가
- ✅ 버전 관리 스크립트 자동 설치

**지원하는 프로젝트 타입**:
- `spring` - Spring Boot / Java / Gradle
- `flutter` - Flutter / Dart
- `react` - React.js / Next.js
- `react-native` - React Native (iOS + Android)
- `react-native-expo` - Expo 기반 React Native
- `node` - Node.js / Express
- `python` - Python / FastAPI / Django
- `basic` - 기본 타입 (버전 관리만)

---

### 매 커밋마다 자동 실행

#### main 브랜치 푸시 시
- ✅ 커밋 메시지 분석 (`feat:`, `fix:`, `docs:` 등)
- ✅ 버전 자동 증가 (1.0.0 → 1.0.1)
- ✅ README 버전 자동 업데이트
- ✅ Git 태그 자동 생성 (`v1.0.1`)
- ✅ 프로젝트별 버전 파일 동기화:
  - Spring: `build.gradle` 또는 `pom.xml`
  - Flutter: `pubspec.yaml`
  - React/Node: `package.json`
  - React Native: `package.json`, `ios/Info.plist`, `android/build.gradle`
  - Python: `pyproject.toml`

#### deploy 브랜치로 PR 생성/머지 시
- ✅ CodeRabbit AI 자동 코드 리뷰
- ✅ 체인지로그 자동 생성 (`CHANGELOG.json`, `CHANGELOG.md`)
- ✅ PR 자동 머지 (리뷰 통과 시)

---

## 🏢 Organization 사용 시 추가 설정

Organization 저장소에서는 아래 3가지 추가 설정이 필요합니다:

### 1. Actions 설정
```
Organization Settings → Actions → General
├── ✅ Allow GitHub Actions to create and approve pull requests
└── ✅ Allow GitHub Actions to merge pull requests
```

### 2. Repository 설정
```
Repository Settings → General → Pull Requests
├── ✅ Allow auto-merge
└── ✅ Allow squash merging
```

### 3. Member 권한 확인
```
Organization Settings → Member privileges
└── Personal access token expiration policy: 조직 정책에 맞게 설정
```

💡 **개인 저장소는 추가 설정 불필요합니다.**

---

## 🧪 동작 확인

### 첫 번째 테스트: 버전 자동 증가

```bash
# main 브랜치에 테스트 커밋
echo "# 테스트" >> TEST.md
git add TEST.md
git commit -m "test: 자동화 테스트"
git push origin main
```

**예상 결과** (GitHub Actions 탭에서 확인):
1. `PROJECT-COMMON-VERSION-CONTROL` 워크플로우 실행
2. 버전 자동 증가 (예: v0.0.0 → v0.0.1)
3. Git 태그 `v0.0.1` 자동 생성
4. README 버전 자동 업데이트

---

### 두 번째 테스트: 체인지로그 자동 생성

```bash
# feature 브랜치 생성 및 작업
git checkout -b feature/test-changelog
echo "# 체인지로그 테스트" >> TEST2.md
git add TEST2.md
git commit -m "feat: 체인지로그 테스트 기능"
git push origin feature/test-changelog

# GitHub에서 deploy 브랜치로 PR 생성
```

**예상 결과**:
1. CodeRabbit AI 자동 리뷰
2. `CHANGELOG.json`, `CHANGELOG.md` 자동 생성
3. PR 자동 머지 (리뷰 통과 시)

---

## 🚨 자주 묻는 질문

### Q1: 워크플로우가 실행되지 않아요

**원인**: `deploy` 브랜치에 워크플로우 파일이 없을 수 있습니다.

**해결**:
```bash
git checkout deploy
ls .github/workflows/

# 파일이 없다면 main에서 복사
git checkout main
git checkout deploy
git merge main
git push origin deploy
```

---

### Q2: 토큰 권한 오류가 발생해요

**증상**:
```
remote: Permission to ... denied to github-actions[bot]
```

**해결**:
1. 토큰이 **Classic** 타입인지 확인 (Fine-grained 아님)
2. `repo`, `workflow` 권한 **모두** 체크되었는지 확인
3. Organization 설정에서 PAT 정책 확인
4. 토큰 만료 날짜 확인

---

### Q3: 버전이 동기화되지 않아요

**해결**: 수동 동기화 실행
```bash
# 현재 버전 상태 확인
.github/scripts/version_manager.sh get

# 모든 파일 동기화
.github/scripts/version_manager.sh sync

# 특정 버전으로 강제 설정
.github/scripts/version_manager.sh set 1.0.0
```

---

### Q4: CodeRabbit 리뷰가 동작하지 않아요

**확인 사항**:
1. `.coderabbit.yaml` 파일이 프로젝트 루트에 있는지 확인
2. CodeRabbit이 저장소에 액세스 권한이 있는지 확인
3. PR에 충분한 변경사항이 있는지 확인 (1줄 이상)

---

### Q5: Spring Boot 프로젝트인데 Nexus 배포 워크플로우가 없어요

**답변**: Nexus 워크플로우는 Spring 타입 선택 시 자동으로 복사됩니다:
- `PROJECT-SPRING-NEXUS-CI.yml`
- `PROJECT-SPRING-NEXUS-PUBLISH.yml`

**추가 설정 필요**:
1. GitHub Secrets에 Nexus 인증 정보 등록:
   - `NEXUS_USERNAME`
   - `NEXUS_PASSWORD`
   - `NEXUS_URL`
2. `gradle.properties` 또는 `build.gradle`에 Nexus 저장소 설정

---

## 📚 추가 문서

- [CONTRIBUTING.md](CONTRIBUTING.md) - 상세 기여 가이드 및 워크플로우 설명
- [CHANGELOG.md](CHANGELOG.md) - 전체 변경 이력
- [README.md](README.md) - 프로젝트 개요 및 빠른 시작

---

## 💡 다음 단계

### 프로젝트 타입 변경하려면?

```bash
# 원격 스크립트로 타입 변경 
bash <(curl -fsSL "https://raw.githubusercontent.com/Cassiiopeia/SUH-DEVOPS-TEMPLATE/main/template_integrator.sh")
```

### 고급 기능 활용

1. **수동 워크플로우 실행**: GitHub Actions 탭에서 `workflow_dispatch` 트리거 활용
2. **멀티 환경 배포**: `version.yml`에 환경별 설정 추가
3. **커스텀 워크플로우**: `.github/workflows/`에 프로젝트별 워크플로우 추가 가능

---

**🎉 축하합니다! 이제 완전 자동화된 DevOps 환경이 구축되었습니다.**

추가 질문이나 문제가 있다면 [이슈를 생성](https://github.com/Cassiiopeia/SUH-DEVOPS-TEMPLATE/issues/new/choose)해 주세요.
