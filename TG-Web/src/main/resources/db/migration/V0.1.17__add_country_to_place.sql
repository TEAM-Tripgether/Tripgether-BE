-- Place 테이블에 country 컬럼 추가
-- ISO 3166-1 alpha-2 형식의 2자리 국가 코드 (KR, US, JP, CN 등)

-- 1. country 컬럼 추가 (기존 레코드에는 기본값 'KR' 설정)
ALTER TABLE place
ADD COLUMN country VARCHAR(2) NOT NULL DEFAULT 'KR';

-- 2. 기본값 제거 (새로운 레코드는 명시적으로 country 입력 필요)
ALTER TABLE place
ALTER COLUMN country DROP DEFAULT;

-- 3. 인덱스 추가 (국가별 검색 성능 향상)
CREATE INDEX idx_place_country ON place(country);

-- 4. CHECK 제약조건 추가 (국가 코드는 2자리 대문자만 허용)
ALTER TABLE place
ADD CONSTRAINT chk_place_country_format
CHECK (country ~ '^[A-Z]{2}$');
