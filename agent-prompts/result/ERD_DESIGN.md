# Tripgether ERD 설계

## 📊 데이터베이스 설계 개요

**데이터베이스**: PostgreSQL  
**설계 방식**: 정규화 기반 관계형 데이터베이스  
**핵심 도메인**: 사용자, 코스, 장소, 결제, 콘텐츠

## 🗂️ 핵심 엔티티 및 관계

### 1. 사용자 도메인 (User Domain)

#### users (사용자 기본 정보)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255), -- 소셜 로그인 시 NULL
    nickname VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    phone_number VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(10),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_social_accounts (소셜 로그인 계정)
```sql
CREATE TABLE user_social_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL, -- 'kakao', 'naver', 'google'
    provider_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provider, provider_id)
);
```

#### user_verifications (본인인증 정보)
```sql
CREATE TABLE user_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    verification_type VARCHAR(50) NOT NULL, -- 'identity', 'phone'
    verification_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'VERIFIED', 'REJECTED'
    verification_data JSONB, -- 인증 관련 데이터
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### seller_accounts (판매자 계정)
```sql
CREATE TABLE seller_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(200),
    business_number VARCHAR(50),
    settlement_account VARCHAR(100) NOT NULL,
    settlement_bank VARCHAR(50) NOT NULL,
    settlement_account_holder VARCHAR(100) NOT NULL,
    is_approved BOOLEAN DEFAULT FALSE,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 장소 도메인 (Place Domain)

#### places (장소 정보)
```sql
CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    category VARCHAR(100), -- 'restaurant', 'attraction', 'accommodation', etc.
    rating DECIMAL(3, 2) DEFAULT 0.0,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### place_images (장소 이미지)
```sql
CREATE TABLE place_images (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT REFERENCES places(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    image_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_saved_places (사용자 저장 장소)
```sql
CREATE TABLE user_saved_places (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    place_id BIGINT REFERENCES places(id) ON DELETE CASCADE,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, place_id)
);
```

### 3. 코스 도메인 (Course Domain)

#### courses (코스 기본 정보)
```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT REFERENCES seller_accounts(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    teaser_content TEXT, -- 티저 콘텐츠
    full_content TEXT, -- 전체 콘텐츠 (구매 후 확인 가능)
    price DECIMAL(10, 2) NOT NULL,
    duration_days INTEGER,
    difficulty_level VARCHAR(20), -- 'EASY', 'MEDIUM', 'HARD'
    category VARCHAR(100),
    status VARCHAR(20) DEFAULT 'DRAFT', -- 'DRAFT', 'PENDING', 'APPROVED', 'REJECTED'
    view_count INTEGER DEFAULT 0,
    purchase_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### course_places (코스-장소 관계)
```sql
CREATE TABLE course_places (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
    place_id BIGINT REFERENCES places(id) ON DELETE CASCADE,
    day_order INTEGER NOT NULL, -- 코스 내 일차
    place_order INTEGER NOT NULL, -- 해당 일차 내 순서
    description TEXT, -- 해당 장소에 대한 설명
    estimated_duration INTEGER, -- 예상 소요 시간 (분)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, day_order, place_order)
);
```

#### course_images (코스 이미지)
```sql
CREATE TABLE course_images (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(20) DEFAULT 'GENERAL', -- 'TEASER', 'GENERAL'
    image_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. 결제 도메인 (Payment Domain)

#### orders (주문 정보)
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'PAID', 'CANCELLED', 'REFUNDED'
    payment_method VARCHAR(50),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### payments (결제 정보)
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    payment_key VARCHAR(100) UNIQUE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'SUCCESS', 'FAILED', 'CANCELLED'
    pg_provider VARCHAR(50), -- 'toss', 'kakao', 'naver'
    pg_transaction_id VARCHAR(100),
    failure_reason TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5. 콘텐츠 도메인 (Content Domain)

#### sns_contents (SNS 공유 콘텐츠)
```sql
CREATE TABLE sns_contents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    original_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(20) NOT NULL, -- 'INSTAGRAM', 'TIKTOK', 'YOUTUBE'
    title VARCHAR(200),
    description TEXT,
    thumbnail_url VARCHAR(500),
    analysis_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'ANALYZING', 'COMPLETED', 'FAILED'
    analysis_result JSONB, -- AI 분석 결과
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### content_extracted_places (콘텐츠에서 추출된 장소)
```sql
CREATE TABLE content_extracted_places (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT REFERENCES sns_contents(id) ON DELETE CASCADE,
    place_id BIGINT REFERENCES places(id) ON DELETE CASCADE,
    confidence_score DECIMAL(3, 2), -- AI 분석 신뢰도
    extraction_method VARCHAR(50), -- 'AI_ANALYSIS', 'MANUAL'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6. 시스템 도메인 (System Domain)

#### course_reviews (코스 리뷰)
```sql
CREATE TABLE course_reviews (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT REFERENCES courses(id) ON DELETE CASCADE,
    buyer_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    is_verified BOOLEAN DEFAULT FALSE, -- 구매 인증된 리뷰
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, buyer_id)
);
```

#### notifications (알림)
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- 'PURCHASE', 'COURSE_APPROVED', 'PAYMENT', etc.
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    related_id BIGINT, -- 관련 엔티티 ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔗 주요 관계 (Relationships)

### 1:1 관계
- `users` ↔ `seller_accounts` (사용자당 하나의 판매자 계정)

### 1:N 관계
- `users` → `user_social_accounts` (사용자당 여러 소셜 계정)
- `users` → `user_verifications` (사용자당 여러 인증)
- `users` → `user_saved_places` (사용자당 여러 저장 장소)
- `users` → `orders` (사용자당 여러 주문)
- `users` → `sns_contents` (사용자당 여러 SNS 콘텐츠)
- `places` → `place_images` (장소당 여러 이미지)
- `places` → `user_saved_places` (장소당 여러 사용자 저장)
- `courses` → `course_places` (코스당 여러 장소)
- `courses` → `course_images` (코스당 여러 이미지)
- `courses` → `orders` (코스당 여러 주문)
- `courses` → `course_reviews` (코스당 여러 리뷰)
- `orders` → `payments` (주문당 여러 결제 시도)
- `sns_contents` → `content_extracted_places` (콘텐츠당 여러 추출 장소)

### N:M 관계
- `courses` ↔ `places` (코스-장소 다대다, `course_places`로 연결)

## 📈 인덱스 설계

### 성능 최적화를 위한 인덱스
```sql
-- 사용자 관련
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nickname ON users(nickname);
CREATE INDEX idx_user_social_accounts_provider ON user_social_accounts(provider, provider_id);

-- 장소 관련
CREATE INDEX idx_places_category ON places(category);
CREATE INDEX idx_places_location ON places(latitude, longitude);
CREATE INDEX idx_user_saved_places_user_id ON user_saved_places(user_id);

-- 코스 관련
CREATE INDEX idx_courses_seller_id ON courses(seller_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_category ON courses(category);
CREATE INDEX idx_courses_price ON courses(price);

-- 주문/결제 관련
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_course_id ON orders(course_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_payments_order_id ON payments(order_id);

-- 콘텐츠 관련
CREATE INDEX idx_sns_contents_user_id ON sns_contents(user_id);
CREATE INDEX idx_sns_contents_analysis_status ON sns_contents(analysis_status);
```

## 🔒 보안 고려사항

### 데이터 암호화
- 개인정보 (휴대폰번호, 이메일) 암호화 저장
- 결제 정보는 PCI DSS 준수
- 비밀번호는 BCrypt 해싱

### 접근 제어
- 구매한 코스 콘텐츠는 구매자만 접근 가능
- 판매자 계정은 승인된 사용자만 접근
- 관리자 기능은 별도 권한 체계

## 📊 데이터 마이그레이션 계획

### Phase 1: 기본 테이블 생성
1. 사용자 관련 테이블
2. 장소 관련 테이블
3. 기본 인덱스 생성

### Phase 2: 코스 및 결제 테이블
1. 코스 관련 테이블
2. 주문/결제 테이블
3. 관계 테이블 생성

### Phase 3: 콘텐츠 및 확장 테이블
1. SNS 콘텐츠 테이블
2. 리뷰 및 알림 테이블
3. 성능 최적화 인덱스

---

**작성일**: 2025-01-15  
**작성자**: Tripgether 개발팀  
**버전**: v1.0.0

