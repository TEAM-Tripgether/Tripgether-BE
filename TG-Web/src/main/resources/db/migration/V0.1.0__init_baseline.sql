-- ===================================================================
-- Tripgether 초기 베이스라인 마이그레이션
-- Version: 0.1.0
-- Description: 기존 테이블 구조 베이스라인 설정
-- ===================================================================

-- 이 파일은 Flyway 초기화를 위한 베이스라인 마이그레이션입니다.
-- baseline-on-migrate: true 설정으로 인해 기존 테이블이 있어도 안전하게 실행됩니다.

-- 베이스라인 확인용 주석
-- 현재 프로젝트 버전: 0.1.0
-- 다음 마이그레이션부터 실제 스키마 변경이 적용됩니다.

-- ===================================================================
-- 1. Member 도메인 테이블
-- ===================================================================

-- Member (회원)
CREATE TABLE IF NOT EXISTS member (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    birth_date DATE,
    gender VARCHAR(10),
    onboarding_status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    tutorial_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    member_role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    profile_image_url TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_member_email ON member(email);
CREATE INDEX IF NOT EXISTS idx_member_name ON member(name);
CREATE INDEX IF NOT EXISTS idx_member_is_deleted ON member(is_deleted);

-- AuthCredential (인증 정보)
CREATE TABLE IF NOT EXISTS auth_credential (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    last_password_change_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_auth_credential_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE INDEX IF NOT EXISTS idx_auth_credential_member ON auth_credential(member_id);

-- Interest (관심사)
CREATE TABLE IF NOT EXISTS interest (
    id UUID PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_interest_category ON interest(category);

-- MemberInterest (회원-관심사 연결)
CREATE TABLE IF NOT EXISTS member_interest (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    interest_id UUID NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_member_interest_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_member_interest_interest FOREIGN KEY (interest_id) REFERENCES interest(id)
);

CREATE INDEX IF NOT EXISTS idx_member_interest_member ON member_interest(member_id);
CREATE INDEX IF NOT EXISTS idx_member_interest_interest ON member_interest(interest_id);

-- Notification (알림)
CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY,
    recipient_member_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID,
    read_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_notification_member FOREIGN KEY (recipient_member_id) REFERENCES member(id)
);

CREATE INDEX IF NOT EXISTS idx_notification_recipient ON notification(recipient_member_id);
CREATE INDEX IF NOT EXISTS idx_notification_read_at ON notification(read_at);

-- ===================================================================
-- 2. Place 도메인 테이블
-- ===================================================================

-- Place (장소)
CREATE TABLE IF NOT EXISTS place (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    latitude NUMERIC(10, 7) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(10, 7) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    business_type VARCHAR(100),
    phone VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_place_business_type ON place(business_type);
CREATE INDEX IF NOT EXISTS idx_place_location ON place(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_place_is_deleted ON place(is_deleted);

-- PlaceExternal (외부 플랫폼 장소 ID)
CREATE TABLE IF NOT EXISTS place_external (
    id UUID PRIMARY KEY,
    place_id UUID NOT NULL,
    platform_source VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_place_external_place FOREIGN KEY (place_id) REFERENCES place(id)
);

CREATE INDEX IF NOT EXISTS idx_place_external_place ON place_external(place_id);
CREATE INDEX IF NOT EXISTS idx_place_external_source ON place_external(platform_source, external_id);

-- PlaceBusinessHour (장소 영업시간)
CREATE TABLE IF NOT EXISTS place_business_hour (
    id UUID PRIMARY KEY,
    place_id UUID NOT NULL,
    weekday VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_place_business_hour_place FOREIGN KEY (place_id) REFERENCES place(id),
    CONSTRAINT uk_place_weekday UNIQUE (place_id, weekday)
);

CREATE INDEX IF NOT EXISTS idx_business_hour_place ON place_business_hour(place_id);

-- PlaceMedia (장소 미디어)
CREATE TABLE IF NOT EXISTS place_media (
    id UUID PRIMARY KEY,
    place_id UUID NOT NULL,
    url TEXT NOT NULL,
    mime_type VARCHAR(100),
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_place_media_place FOREIGN KEY (place_id) REFERENCES place(id)
);

CREATE INDEX IF NOT EXISTS idx_place_media_place ON place_media(place_id);
CREATE INDEX IF NOT EXISTS idx_place_media_position ON place_media(position);

-- Folder (폴더)
CREATE TABLE IF NOT EXISTS folder (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL DEFAULT '제목 없음',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    share_link TEXT,
    thumbnail_url TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_folder_owner FOREIGN KEY (owner_id) REFERENCES member(id)
);

CREATE INDEX IF NOT EXISTS idx_folder_owner ON folder(owner_id);
CREATE INDEX IF NOT EXISTS idx_folder_is_deleted ON folder(is_deleted);

-- FolderPlace (폴더-장소 연결)
CREATE TABLE IF NOT EXISTS folder_place (
    id UUID PRIMARY KEY,
    folder_id UUID NOT NULL,
    place_id UUID NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_folder_place_folder FOREIGN KEY (folder_id) REFERENCES folder(id),
    CONSTRAINT fk_folder_place_place FOREIGN KEY (place_id) REFERENCES place(id)
);

CREATE INDEX IF NOT EXISTS idx_folder_place_folder ON folder_place(folder_id);
CREATE INDEX IF NOT EXISTS idx_folder_place_place ON folder_place(place_id);

-- ===================================================================
-- 3. SNS Content 도메인 테이블
-- ===================================================================

-- Content (SNS 콘텐츠)
CREATE TABLE IF NOT EXISTS content (
    id UUID PRIMARY KEY,
    platform VARCHAR(50) NOT NULL,
    platform_uploader VARCHAR(255) NOT NULL,
    caption TEXT NOT NULL,
    thumbnail_url TEXT NOT NULL,
    original_url TEXT NOT NULL,
    title VARCHAR(500) NOT NULL,
    summary TEXT,
    last_checked_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_content_platform ON content(platform);
CREATE INDEX IF NOT EXISTS idx_content_is_deleted ON content(is_deleted);

-- ContentMedia (콘텐츠 미디어)
CREATE TABLE IF NOT EXISTS content_media (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL,
    url TEXT NOT NULL,
    mime_type VARCHAR(100),
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_content_media_content FOREIGN KEY (content_id) REFERENCES content(id)
);

CREATE INDEX IF NOT EXISTS idx_content_media_content ON content_media(content_id);
CREATE INDEX IF NOT EXISTS idx_content_media_position ON content_media(position);

-- ContentPlace (콘텐츠-장소 연결)
CREATE TABLE IF NOT EXISTS content_place (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL,
    place_id UUID NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_content_place_content FOREIGN KEY (content_id) REFERENCES content(id),
    CONSTRAINT fk_content_place_place FOREIGN KEY (place_id) REFERENCES place(id),
    CONSTRAINT uk_content_place_pair UNIQUE (content_id, place_id),
    CONSTRAINT uk_content_place_pos UNIQUE (content_id, position)
);

CREATE INDEX IF NOT EXISTS idx_content_place_content ON content_place(content_id);
CREATE INDEX IF NOT EXISTS idx_content_place_place ON content_place(place_id);

-- ===================================================================
-- 4. Application 도메인 테이블
-- ===================================================================

-- AiJob (AI 작업)
CREATE TABLE IF NOT EXISTS ai_job (
    id UUID PRIMARY KEY,
    version BIGINT,
    content_id UUID NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    attempt INTEGER NOT NULL DEFAULT 0,
    max_attempt INTEGER NOT NULL DEFAULT 3,
    result JSONB,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_ai_job_content FOREIGN KEY (content_id) REFERENCES content(id)
);

CREATE INDEX IF NOT EXISTS idx_ai_job_content ON ai_job(content_id);
CREATE INDEX IF NOT EXISTS idx_ai_job_status ON ai_job(status);
CREATE INDEX IF NOT EXISTS idx_ai_job_type ON ai_job(job_type);

-- ===================================================================
-- 베이스라인 마이그레이션 완료
-- ===================================================================

COMMENT ON TABLE member IS '회원 정보';
COMMENT ON TABLE place IS '장소 정보';
COMMENT ON TABLE folder IS '사용자 폴더';
COMMENT ON TABLE content IS 'SNS 콘텐츠';
COMMENT ON TABLE ai_job IS 'AI 분석 작업';
