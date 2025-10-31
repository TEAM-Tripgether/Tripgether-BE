-- ===================================================================
-- AI 서버 연동 기능 추가
-- ===================================================================
-- Issue: #48
-- Description: AI 서버 연동 API 구현 - 장소 추출 요청 및 Webhook Callback 처리
-- Date: 2025-10-31
-- ===================================================================

-- 1. Place 테이블에 country 컬럼 추가
-- ISO 3166-1 alpha-2 형식의 2자리 국가 코드 (KR, US, JP, CN 등)

ALTER TABLE place
ADD COLUMN country VARCHAR(2) NOT NULL DEFAULT 'KR';

ALTER TABLE place
ALTER COLUMN country DROP DEFAULT;

CREATE INDEX idx_place_country ON place(country);

ALTER TABLE place
ADD CONSTRAINT chk_place_country_format
CHECK (country ~ '^[A-Z]{2}$');


-- 2. Content 테이블에 status 컬럼 추가
-- AI 분석 상태 추적용: PENDING, ANALYZING, COMPLETED, FAILED, DELETED

ALTER TABLE content
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE content
ALTER COLUMN status DROP DEFAULT;

CREATE INDEX idx_content_status ON content(status);

ALTER TABLE content
ADD CONSTRAINT chk_content_status
CHECK (status IN ('PENDING', 'ANALYZING', 'COMPLETED', 'FAILED', 'DELETED'));
