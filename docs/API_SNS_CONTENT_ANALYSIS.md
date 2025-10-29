# SNS 콘텐츠 분석 API 명세서

## 📊 개요

SNS URL을 분석하여 콘텐츠에 포함된 장소 정보를 자동으로 추출하는 비동기 API 시스템입니다.  
AI 서버와 Webhook 방식으로 통신하며, 프론트엔드는 폴링을 통해 분석 결과를 조회합니다.

**통신 방식**: 비동기 (Webhook Callback)  
**처리 시간**: 평균 30초 ~ 수분  
**지원 플랫폼**: Instagram, YouTube, TikTok, Facebook, Twitter

---

## 🔄 전체 플로우

```
1. 프론트엔드 → 백엔드
   POST /api/content/analyze
   { "snsUrl": "https://instagram.com/p/ABC123/", "platform": "INSTAGRAM" }
   
2. 백엔드 → 프론트엔드 (즉시 응답)
   202 Accepted
   { "contentId": "uuid-1", "status": "PENDING", "message": "분석 요청이 접수되었습니다." }

3. 백엔드 → AI 서버 (비동기 요청)
   POST {AI_SERVER_URL}/api/extract-places
   { "contentId": "uuid-1", "snsUrl": "https://instagram.com/p/ABC123/" }
   Header: X-API-Key: {API_KEY}

4. AI 서버 → 백엔드 (즉시 응답)
   202 Accepted
   { "contentId": "uuid-1", "status": "ACCEPTED" }

---
[AI 서버가 30초~수분간 장소 추출 작업 수행...]
---

5. AI 서버 → 백엔드 (Webhook Callback)
   POST /api/ai/callback
   { "contentId": "uuid-1", "resultStatus": "SUCCESS", "contentInfo": {...}, "places": [...] }
   Header: X-AI-Server-Key: {CALLBACK_KEY}

6. 백엔드 처리
   - address → Geocoding (좌표 변환)
   - Place 엔티티 저장
   - ContentPlace 연결
   - AiJob 상태 업데이트 (COMPLETED)

7. 프론트엔드 → 백엔드 (폴링, 3초 간격)
   GET /api/content/{contentId}/status
   
8. 백엔드 → 프론트엔드 (완료 시)
   200 OK
   { "contentId": "uuid-1", "status": "COMPLETED", "places": [...] }
```

---

## 📱 프론트엔드 ↔ 백엔드 API

### 1.1 SNS URL 분석 요청

**엔드포인트**: `POST /api/content/analyze`  
**인증**: JWT 필요 (`Authorization: Bearer {token}`)  
**설명**: SNS URL을 받아 AI 서버에 분석 요청을 보내고 즉시 응답합니다.

#### Request

```json
{
  "snsUrl": "https://www.instagram.com/p/ABC123/",
  "platform": "INSTAGRAM"
}
```

**필드 설명**:

| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| `snsUrl` | String | ✅ | SNS 콘텐츠 URL | `https://www.instagram.com/p/ABC123/` |
| `platform` | String (Enum) | ✅ | SNS 플랫폼 | `INSTAGRAM`, `YOUTUBE`, `TIKTOK`, `FACEBOOK`, `TWITTER` |

#### Response (202 Accepted)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "분석 요청이 접수되었습니다."
}
```

**필드 설명**:

| 필드 | 타입 | 설명 |
|------|------|------|
| `contentId` | UUID | 생성된 콘텐츠 ID (상태 조회 시 사용) |
| `status` | String | 현재 상태 (`PENDING`) |
| `message` | String | 사용자 안내 메시지 |

#### Error Response

```json
{
  "message": "올바른 SNS URL이 아닙니다."
}
```

**가능한 에러 코드**:
- `400 Bad Request`: 잘못된 URL 형식
- `401 Unauthorized`: 인증 토큰 없음/만료
- `409 Conflict`: 이미 분석 중인 URL
- `500 Internal Server Error`: 서버 내부 오류

---

### 1.2 분석 상태 조회 (폴링용)

**엔드포인트**: `GET /api/content/{contentId}/status`  
**인증**: JWT 필요  
**설명**: 분석 진행 상태 및 완료된 장소 목록을 조회합니다.  
**권장 폴링 주기**: 3초 간격, 최대 60회 (3분)

#### Request

```http
GET /api/content/550e8400-e29b-41d4-a716-446655440000/status
Authorization: Bearer {access_token}
```

#### Response 1: 분석 대기 중 (200 OK)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING"
}
```

#### Response 2: 분석 진행 중 (200 OK)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ANALYZING"
}
```

#### Response 3: 분석 완료 (200 OK)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "places": [
    {
      "id": "place-uuid-1",
      "name": "명동 교자",
      "address": "서울특별시 중구 명동길 29",
      "latitude": 37.5635,
      "longitude": 126.9850,
      "businessType": "한식당",
      "phone": "02-123-4567",
      "description": "칼국수와 만두로 유명한 맛집"
    },
    {
      "id": "place-uuid-2",
      "name": "홍대 앞 거리",
      "address": "서울특별시 마포구 양화로",
      "latitude": 37.5563,
      "longitude": 126.9236,
      "businessType": "관광지",
      "phone": null,
      "description": "젊음의 거리, 각종 공연과 카페"
    }
  ]
}
```

#### Response 4: 분석 실패 (200 OK)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "FAILED",
  "errorMessage": "콘텐츠에서 장소 정보를 찾을 수 없습니다."
}
```

**필드 설명**:

| 필드 | 타입 | 포함 조건 | 설명 |
|------|------|----------|------|
| `contentId` | UUID | 항상 | Content 식별자 |
| `status` | String (Enum) | 항상 | 분석 상태 |
| `places` | List\<Place\> | COMPLETED 시만 | 추출된 장소 목록 |
| `errorMessage` | String | FAILED 시만 | 실패 사유 |

**Status 값**:

| 값 | 설명 |
|------|------|
| `PENDING` | 분석 대기 중 |
| `ANALYZING` | 분석 진행 중 |
| `COMPLETED` | 분석 완료 (places 포함) |
| `FAILED` | 분석 실패 (errorMessage 포함) |
| `DELETED` | 삭제됨 |

**Place 객체**:

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| `id` | UUID | ❌ | 장소 ID |
| `name` | String | ❌ | 장소명 |
| `address` | String | ✅ | 주소 |
| `latitude` | Decimal | ❌ | 위도 (백엔드에서 Geocoding) |
| `longitude` | Decimal | ❌ | 경도 (백엔드에서 Geocoding) |
| `businessType` | String | ✅ | 업종 |
| `phone` | String | ✅ | 전화번호 |
| `description` | String | ✅ | 장소 설명 |

---

## 🤖 백엔드 ↔ AI 서버 API

### 2.1 장소 추출 요청

**엔드포인트**: `POST {AI_SERVER_BASE_URL}/api/extract-places`  
**인증**: API Key (Header: `X-API-Key`)  
**설명**: 백엔드가 AI 서버에 장소 추출 작업을 요청합니다.

#### Request

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "snsUrl": "https://www.instagram.com/p/ABC123/"
}
```

**Header**:
```
X-API-Key: {AI_SERVER_API_KEY}
Content-Type: application/json
```

**필드 설명**:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `contentId` | UUID | ✅ | 백엔드에서 생성한 Content ID |
| `snsUrl` | String | ✅ | 분석할 SNS URL |

#### Response (202 Accepted)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED",
  "message": "분석 작업이 접수되었습니다."
}
```

---

### 2.2 AI 서버 → 백엔드 Webhook Callback

**엔드포인트**: `POST /api/ai/callback`  
**인증**: API Key 검증 (`X-AI-Server-Key`)  
**설명**: AI 서버가 분석 완료 후 백엔드에 결과를 전송합니다.

#### Request (성공)

```json
{
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "resultStatus": "SUCCESS",
  "contentInfo": {
    "title": "서울 여행 브이로그",
    "caption": "서울 명동과 홍대를 다녀왔어요! #서울여행 #맛집투어",
    "thumbnailUrl": "https://cdn.instagram.com/v/t51.2885-15/...",
    "platformUploader": "travel_lover"
  },
  "places": [
    {
      "name": "명동 교자",
      "address": "서울특별시 중구 명동길 29",
      "phone": "02-123-4567",
      "description": "칼국수와 만두로 유명한 맛집"
    },
    {
      "name": "홍대 앞 거리",
      "address": "서울특별시 마포구 양화로",
      "phone": null,
      "description": "젊음의 거리, 각종 공연과 카페"
    }
  ]
}
```

**Header**:
```
X-AI-Server-Key: {AI_CALLBACK_API_KEY}
Content-Type: application/json
```

**필드 설명**:

**공통 필드**:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `contentId` | UUID | ✅ | Content 식별자 |
| `resultStatus` | String (Enum) | ✅ | 처리 결과 상태 |

**성공 시 (resultStatus=SUCCESS)**:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `contentInfo` | Object | ✅ | SNS 콘텐츠 정보 |
| `contentInfo.title` | String | ✅ | 콘텐츠 제목 (최대 500자) |
| `contentInfo.caption` | String | ✅ | 콘텐츠 본문/캡션 |
| `contentInfo.thumbnailUrl` | String | ✅ | 썸네일 이미지 URL |
| `contentInfo.platformUploader` | String | ✅ | 업로더 아이디 (최대 255자) |
| `places` | Array | ✅ | 추출된 장소 목록 (빈 배열 가능) |

**Place 객체**:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | ✅ | 장소명 (최대 255자) |
| `address` | String | ✅ | 주소 (최대 500자) - **백엔드가 이걸로 좌표 찾음** |
| `phone` | String | ❌ | 전화번호 (최대 50자) |
| `description` | String | ❌ | 장소 설명 |

**resultStatus 값**:

| 값 | 의미 | contentInfo | places | 백엔드 처리 |
|------|------|-------------|--------|------------|
| `SUCCESS` | 분석 성공 | ✅ 필수 | ✅ 필수 | Place 저장, COMPLETED |

#### Response (백엔드 → AI 서버)

```json
{
  "received": true,
  "contentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 🔒 보안 및 인증

### 3.1 프론트엔드 → 백엔드

**방식**: JWT Bearer Token  
**헤더**: `Authorization: Bearer {access_token}`  
**검증**: Spring Security (기존 인증 시스템)

### 3.2 백엔드 → AI 서버

**방식**: API Key  
**헤더**: `X-API-Key: {AI_SERVER_API_KEY}`

**API Key 발급 방법**:
```bash
# OpenSSL로 생성
openssl rand -hex 32
# 결과: a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456
```

**환경변수 설정**:
```bash
# 개발 환경
export AI_SERVER_API_KEY="dev-backend-to-ai-key-12345"

# 운영 환경 (Kubernetes Secret 등 사용 권장)
kubectl create secret generic ai-server-secrets \
  --from-literal=api-key='prod-secure-key-xyz...'
```

### 3.3 AI 서버 → 백엔드 (Webhook Callback)

**방식**: API Key + IP 화이트리스트 (권장)  
**헤더**: `X-AI-Server-Key: {AI_CALLBACK_API_KEY}`

**백엔드 검증 예시**:
```java
@PostMapping("/api/ai/callback")
public ResponseEntity<Void> handleCallback(
    @RequestHeader("X-AI-Server-Key") String apiKey,
    @RequestBody AiAnalyzeCallbackRequest request) {
    
    // API Key 검증
    if (!apiKey.equals(aiServerProperties.getCallbackApiKey())) {
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    
    // 처리...
}
```

---

## ⚙️ 환경 설정

### application.yml

```yaml
# AI 서버 설정
ai-server:
  base-url: http://ai-server.example.com
  api-key: ${AI_SERVER_API_KEY}  # 백엔드 → AI 서버 요청 시 사용
  callback-api-key: ${AI_CALLBACK_API_KEY}  # AI 서버 → 백엔드 Callback 검증용
  timeout: 60000  # 60초
  
# Geocoding API 설정 (주소 → 좌표 변환용)
geocoding:
  provider: kakao  # kakao, google, naver 중 선택
  api-key: ${GEOCODING_API_KEY}
```

### application-dev.yml

```yaml
ai-server:
  base-url: http://localhost:8000
  api-key: dev-backend-to-ai-key
  callback-api-key: dev-ai-to-backend-key

geocoding:
  provider: kakao
  api-key: dev-kakao-api-key
```

### application-prod.yml

```yaml
ai-server:
  base-url: https://ai.tripgether.internal
  api-key: ${AI_SERVER_API_KEY}
  callback-api-key: ${AI_CALLBACK_API_KEY}

geocoding:
  provider: kakao
  api-key: ${KAKAO_API_KEY}
```

---

## 🗺️ Geocoding (주소 → 좌표 변환)

### 백엔드 처리 플로우

```
1. AI 서버로부터 address 수신
2. Geocoding API 호출 (Kakao/Google/Naver)
3. latitude, longitude 획득
4. Place 엔티티에 저장
```

### Kakao Geocoding API 예시

**엔드포인트**: `GET https://dapi.kakao.com/v2/local/search/address.json`

**Request**:
```http
GET https://dapi.kakao.com/v2/local/search/address.json?query=서울특별시 중구 명동길 29
Authorization: KakaoAK {REST_API_KEY}
```

**Response**:
```json
{
  "documents": [
    {
      "address_name": "서울특별시 중구 명동길 29",
      "x": "126.985012",
      "y": "37.563512"
    }
  ]
}
```

### 실패 처리

- Geocoding 실패 시: `latitude`, `longitude`를 `null`로 저장
- 프론트엔드에서 좌표 없는 장소는 지도에 표시 안 함 (리스트만 표시)

---

## 📊 프론트엔드 폴링 가이드

### 권장 폴링 전략

```javascript
/**
 * SNS URL 분석 및 폴링
 * 
 * @param snsUrl - 분석할 SNS URL
 * @param platform - SNS 플랫폼 (INSTAGRAM, YOUTUBE 등)
 * @returns 분석 완료된 장소 목록
 */
async function analyzeSnsUrl(snsUrl, platform) {
  // 1. 분석 요청
  const { contentId } = await fetch('/api/content/analyze', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ snsUrl, platform })
  }).then(res => res.json());

  // 2. 폴링 시작
  return new Promise((resolve, reject) => {
    let attempts = 0;
    const maxAttempts = 60;  // 3분 (3초 * 60회)
    
    const interval = setInterval(async () => {
      attempts++;
      
      try {
        const status = await fetch(`/api/content/${contentId}/status`, {
          headers: { 'Authorization': `Bearer ${token}` }
        }).then(res => res.json());
        
        if (status.status === 'COMPLETED') {
          clearInterval(interval);
          resolve(status);  // 성공!
        }
        
        if (status.status === 'FAILED') {
          clearInterval(interval);
          reject(new Error(status.errorMessage));
        }
        
        if (attempts >= maxAttempts) {
          clearInterval(interval);
          reject(new Error('분석 시간이 초과되었습니다.'));
        }
      } catch (error) {
        clearInterval(interval);
        reject(error);
      }
    }, 3000);  // 3초마다
  });
}

// 사용 예시
try {
  const result = await analyzeSnsUrl(
    'https://instagram.com/p/ABC123/',
    'INSTAGRAM'
  );
  console.log('분석 완료!', result.places);
} catch (error) {
  console.error('분석 실패:', error.message);
}
```

### UX 권장사항

- **로딩 표시**: "AI가 장소를 분석하고 있습니다... (30초~2분 소요)"
- **취소 버튼**: 사용자가 대기 중 취소 가능하도록
- **타임아웃 안내**: 3분 초과 시 "시간이 오래 걸립니다. 나중에 다시 확인해주세요."

---

## 🧪 테스트 시나리오

### 시나리오 1: 정상 처리 (장소 2개)

```
1. 프론트 → 백엔드: POST /api/content/analyze
   Request: { "snsUrl": "https://instagram.com/p/ABC123/", "platform": "INSTAGRAM" }
   Response: { "contentId": "uuid-1", "status": "PENDING" }

2. 백엔드 → AI 서버: POST /api/extract-places
   Request: { "contentId": "uuid-1", "snsUrl": "https://instagram.com/p/ABC123/" }
   Header: X-API-Key: a1b2c3d4...
   Response: { "contentId": "uuid-1", "status": "ACCEPTED" }

3. [45초 후] AI 서버 → 백엔드: POST /api/ai/callback
   Request: { "contentId": "uuid-1", "resultStatus": "SUCCESS", "places": [...] }
   Header: X-AI-Server-Key: fedcba09...
   Response: { "received": true }

4. 백엔드 처리:
   - Content 업데이트
   - Geocoding 2회 호출
   - Place 2개 저장
   - ContentPlace 2개 연결
   - AiJob → COMPLETED

5. 프론트 → 백엔드: GET /api/content/uuid-1/status (15회 폴링)
   Response: { "contentId": "uuid-1", "status": "COMPLETED", "places": [...] }
```

---

## 📋 API Key 관리 가이드

### API Key 종류 (총 3개)

| 이름 | 용도 | 발급자 | 저장 위치 |
|------|------|--------|----------|
| `AI_SERVER_API_KEY` | 백엔드 → AI 서버 요청 인증 | 백엔드 팀 | 백엔드 환경변수 + AI 서버 설정 |
| `AI_CALLBACK_API_KEY` | AI 서버 → 백엔드 Callback 인증 | 백엔드 팀 | 백엔드 환경변수 + AI 서버 설정 |
| `GEOCODING_API_KEY` | 백엔드 → Kakao/Google API | Kakao/Google | 백엔드 환경변수 |

### 발급 절차

#### Step 1: 백엔드 팀이 API Key 생성

```bash
# AI_SERVER_API_KEY 생성
openssl rand -hex 32
# 예: a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456

# AI_CALLBACK_API_KEY 생성
openssl rand -hex 32
# 예: fedcba0987654321fedcba0987654321fedcba0987654321fedcba098765432
```

#### Step 2: AI 팀에게 전달

**보안 채널 사용 (Slack DM, 암호화된 이메일 등)**

```
AI 서버 연동을 위한 API Key 전달드립니다.

1. 백엔드 → AI 서버 요청 시 사용할 Key:
   X-API-Key: a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456

2. AI 서버 → 백엔드 Callback 시 사용할 Key:
   X-AI-Server-Key: fedcba0987654321fedcba0987654321fedcba0987654321fedcba098765432

3. Callback URL:
   개발: http://localhost:8080/api/ai/callback
   운영: https://api.tripgether.suhsaechan.kr/api/ai/callback
```

#### Step 3: 양측 설정 파일에 저장

**백엔드 (application.yml)**:
```yaml
ai-server:
  api-key: ${AI_SERVER_API_KEY}
  callback-api-key: ${AI_CALLBACK_API_KEY}
```

**AI 서버 (config.yaml 예시)**:
```yaml
backend:
  callback_url: https://api.tripgether.suhsaechan.kr/api/ai/callback
  callback_api_key: fedcba0987654321...  # 백엔드가 검증할 키
  
allowed_api_keys:
  - a1b2c3d4e5f6789012345678...  # 백엔드 요청 검증용
```

#### Step 4: Geocoding API Key 발급

**Kakao 예시**:
1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 애플리케이션 생성
3. REST API 키 복사
4. 백엔드 환경변수에 저장

---

## ⚠️ 트러블슈팅

### 1. API Key 불일치

**증상**:
```json
{
  "message": "인증에 실패했습니다."
}
```

**해결 방법**:
- 백엔드와 AI 서버의 API Key 일치 여부 확인
- 환경변수가 제대로 로드되었는지 확인
- 헤더 이름 확인 (`X-API-Key`, `X-AI-Server-Key`)

### 2. Callback URL 접근 불가

**증상**:
- AI 서버에서 Callback 호출 실패
- 백엔드 로그에 Callback 수신 기록 없음

**해결 방법**:
- AI 서버에서 백엔드 URL 접근 가능 여부 확인
- 방화벽 설정 확인
- Callback URL이 올바른지 확인 (개발/운영 환경)

### 3. Geocoding 실패

**증상**:
- Place 저장 시 좌표가 null
- 지도에 장소 표시 안 됨

**해결 방법**:
- Geocoding API Key 유효성 확인
- API 요청 제한 확인 (Kakao: 하루 300,000건)
- 주소 형식 확인 (도로명 주소 권장)

### 4. 폴링 타임아웃

**증상**:
- 3분 경과 후에도 ANALYZING 상태

**해결 방법**:
- AI 서버 로그 확인
- Callback 전송 실패 여부 확인
- 수동으로 상태 조회 (DB 또는 관리자 API)

---

## 📈 모니터링 및 로깅

### 백엔드 로그 예시

```
[2025-10-28 10:15:00] [INFO] SNS URL 분석 요청 수신: contentId=uuid-1, url=https://instagram.com/p/ABC123/
[2025-10-28 10:15:01] [INFO] AI 서버 요청 성공: contentId=uuid-1
[2025-10-28 10:15:45] [INFO] AI Callback 수신: contentId=uuid-1, resultStatus=SUCCESS
[2025-10-28 10:15:46] [INFO] Geocoding 성공: address=서울특별시 중구 명동길 29, lat=37.5635, lng=126.9850
[2025-10-28 10:15:47] [INFO] Place 저장 완료: placeId=place-uuid-1, name=명동 교자
[2025-10-28 10:15:48] [SUCCESS] 분석 완료: contentId=uuid-1, places=2개
```

### 모니터링 지표

- **분석 요청 수**: 시간당/일별
- **평균 처리 시간**: AI 서버 응답 시간
- **성공률**: SUCCESS / 전체 요청
- **Geocoding 성공률**: 좌표 변환 성공 비율
- **폴링 횟수**: 평균 폴링 횟수

---

## ✅ 체크리스트

### AI 팀 확인 사항
- [ ] AI 서버 Base URL (개발/운영 환경)
- [ ] API Key 수신 및 설정 완료
- [ ] Callback URL 접근 가능 여부 확인
- [ ] 예상 평균 처리 시간
- [ ] 최대 동시 처리 요청 수
- [ ] `resultStatus: SUCCESS` 시 필드 포함 확인

### 백엔드 팀 확인 사항
- [ ] API Key 생성 및 전달 완료
- [ ] Geocoding API Key 발급 완료
- [ ] 환경변수 설정 완료
- [ ] Webhook Callback 엔드포인트 구현
- [ ] Security 설정 (Callback URL 인증 제외)

### 프론트엔드 팀 확인 사항
- [ ] 폴링 로직 구현 (3초 간격, 60회)
- [ ] 로딩 UI 구현
- [ ] 에러 처리 (타임아웃, 실패)
- [ ] 장소 목록 표시 (지도 + 리스트)

---

## 📞 문의 및 지원

API 관련 문의사항이나 개선 제안이 있으시면 GitHub Issue를 생성해주세요.

**관련 파일**:
- API 명세: `docs/API_SNS_CONTENT_ANALYSIS.md`
- 프로젝트 구조: `agent-prompts/result/PROJECT_STRUCTURE_GUIDE.md`

---

**작성일**: 2025-10-28  
**API 버전**: v1.0  
**작성자**: Tripgether Backend Team



