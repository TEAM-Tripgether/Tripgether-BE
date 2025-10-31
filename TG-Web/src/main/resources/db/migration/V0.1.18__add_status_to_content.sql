-- Content 테이블에 status 컬럼 추가
-- AI 분석 상태 추적용: PENDING, ANALYZING, COMPLETED, FAILED, DELETED

-- 1. status 컬럼 추가 (기존 레코드는 PENDING으로 설정)
ALTER TABLE content
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 2. 기본값 제거 (새로운 레코드는 명시적으로 status 입력 필요)
ALTER TABLE content
ALTER COLUMN status DROP DEFAULT;

-- 3. 인덱스 추가 (status별 조회 성능 향상)
CREATE INDEX idx_content_status ON content(status);

-- 4. CHECK 제약조건 추가 (허용된 status 값만 입력 가능)
ALTER TABLE content
ADD CONSTRAINT chk_content_status
CHECK (status IN ('PENDING', 'ANALYZING', 'COMPLETED', 'FAILED', 'DELETED'));
