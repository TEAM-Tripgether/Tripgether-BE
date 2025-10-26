-- ===================================================================
-- 미디어 구조 간소화 마이그레이션 (완전 자가 완결형)
-- Version: 0.1.5
-- Description: Media 테이블 제거 및 직접 URL 저장 방식으로 변경
-- 
-- ===================================================================

-- ===================================================================
-- PART 1: 의존 테이블 생성 (없으면)
-- ===================================================================

-- 1-1. Member 테이블 (place_media, content_media가 간접적으로 의존)
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

-- 1-2. Place 테이블 (place_media가 직접 의존)
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

-- 1-3. Content 테이블 (content_media가 직접 의존)
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

-- 1-4. Folder 테이블 (thumbnail_url 추가 대상)
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
    deleted_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_folder_owner ON folder(owner_id);
CREATE INDEX IF NOT EXISTS idx_folder_is_deleted ON folder(is_deleted);

-- 1-5. folder의 owner_id 외래키 추가 (member 테이블 존재 보장 후)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_folder_owner' 
        AND table_name = 'folder'
    ) THEN
        ALTER TABLE folder 
        ADD CONSTRAINT fk_folder_owner 
        FOREIGN KEY (owner_id) REFERENCES member(id);
    END IF;
END $$;

-- ===================================================================
-- PART 2: place_media 테이블 생성 및 수정
-- ===================================================================

-- 2-1. place_media 테이블 생성 (없으면)
CREATE TABLE IF NOT EXISTS place_media (
    id UUID PRIMARY KEY,
    place_id UUID NOT NULL,
    url TEXT NOT NULL,
    mime_type VARCHAR(100),
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 2-2. place_media 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_place_media_place ON place_media(place_id);
CREATE INDEX IF NOT EXISTS idx_place_media_position ON place_media(position);

-- 2-3. place_media 외래키 추가 (place 테이블 존재 보장 후)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_place_media_place' 
        AND table_name = 'place_media'
    ) THEN
        ALTER TABLE place_media 
        ADD CONSTRAINT fk_place_media_place 
        FOREIGN KEY (place_id) REFERENCES place(id);
    END IF;
END $$;

-- 2-4. 기존 media_id 컬럼이 있다면 데이터 마이그레이션 후 삭제
DO $$
BEGIN
    -- media_id 컬럼이 존재하는지 확인
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'place_media' 
        AND column_name = 'media_id'
    ) THEN
        -- 기존 media 테이블이 있고 데이터가 있다면 마이그레이션
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'media') THEN
            -- url이 비어있는 레코드만 media 테이블에서 가져옴
            UPDATE place_media pm
            SET url = m.url,
                mime_type = m.mime_type
            FROM media m
            WHERE pm.media_id = m.id
            AND (pm.url IS NULL OR pm.url = '');
        END IF;
        
        -- media_id 컬럼 삭제
        ALTER TABLE place_media DROP COLUMN media_id;
    END IF;
END $$;

-- ===================================================================
-- PART 3: content_media 테이블 생성 및 수정
-- ===================================================================

-- 3-1. content_media 테이블 생성 (없으면)
CREATE TABLE IF NOT EXISTS content_media (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL,
    url TEXT NOT NULL,
    mime_type VARCHAR(100),
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 3-2. content_media 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_content_media_content ON content_media(content_id);
CREATE INDEX IF NOT EXISTS idx_content_media_position ON content_media(position);

-- 3-3. content_media 외래키 추가 (content 테이블 존재 보장 후)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_content_media_content' 
        AND table_name = 'content_media'
    ) THEN
        ALTER TABLE content_media 
        ADD CONSTRAINT fk_content_media_content 
        FOREIGN KEY (content_id) REFERENCES content(id);
    END IF;
END $$;

-- 3-4. 기존 media_id 컬럼이 있다면 데이터 마이그레이션 후 삭제
DO $$
BEGIN
    -- media_id 컬럼이 존재하는지 확인
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'content_media' 
        AND column_name = 'media_id'
    ) THEN
        -- 기존 media 테이블이 있고 데이터가 있다면 마이그레이션
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'media') THEN
            -- url이 비어있는 레코드만 media 테이블에서 가져옴
            UPDATE content_media cm
            SET url = m.url,
                mime_type = m.mime_type
            FROM media m
            WHERE cm.media_id = m.id
            AND (cm.url IS NULL OR cm.url = '');
        END IF;
        
        -- media_id 컬럼 삭제
        ALTER TABLE content_media DROP COLUMN media_id;
    END IF;
END $$;

-- ===================================================================
-- PART 4: member 테이블에 profile_image_url 추가 (이미 있으면 스킵)
-- ===================================================================

-- member 테이블은 이미 위에서 profile_image_url 포함하여 생성됨
-- 기존 테이블에 컬럼이 없는 경우만 추가
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'member' 
        AND column_name = 'profile_image_url'
    ) THEN
        ALTER TABLE member ADD COLUMN profile_image_url TEXT;
    END IF;
END $$;

-- ===================================================================
-- PART 5: folder 테이블에 thumbnail_url 추가 (이미 있으면 스킵)
-- ===================================================================

-- folder 테이블은 이미 위에서 thumbnail_url 포함하여 생성됨
-- 기존 테이블에 컬럼이 없는 경우만 추가
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'folder' 
        AND column_name = 'thumbnail_url'
    ) THEN
        ALTER TABLE folder ADD COLUMN thumbnail_url TEXT;
    END IF;
END $$;

-- ===================================================================
-- PART 6: 구 Media 테이블 삭제
-- ===================================================================

-- 모든 마이그레이션이 완료된 후 media 테이블 삭제
DROP TABLE IF EXISTS media CASCADE;

-- ===================================================================
-- 마이그레이션 완료
-- ===================================================================

COMMENT ON TABLE place_media IS '장소 미디어 (직접 URL 저장)';
COMMENT ON TABLE content_media IS '콘텐츠 미디어 (직접 URL 저장)';
COMMENT ON COLUMN member.profile_image_url IS '회원 프로필 이미지 URL';
COMMENT ON COLUMN folder.thumbnail_url IS '폴더 썸네일 이미지 URL';
