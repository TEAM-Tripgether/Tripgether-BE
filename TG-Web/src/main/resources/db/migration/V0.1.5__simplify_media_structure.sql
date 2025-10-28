-- ===================================================================
-- 미디어 구조 간소화 마이그레이션
-- Version: 0.1.5
-- Description: Media 테이블 제거 및 직접 URL 저장 방식으로 변경
-- 
-- 전제조건:
--   - JPA가 테이블/컬럼 생성을 자동으로 처리 (ddl-auto: update)
--   - 이 스크립트는 JPA로 불가능한 작업만 수행
-- ===================================================================

-- ===================================================================
-- PART 1: place_media 데이터 마이그레이션
-- ===================================================================

-- 1-1. media_id 컬럼이 존재하면 데이터 마이그레이션 후 삭제
DO $$
BEGIN
    -- media_id 컬럼 존재 여부 확인
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'place_media' 
        AND column_name = 'media_id'
    ) THEN
        RAISE NOTICE 'place_media.media_id 컬럼 발견 - 데이터 마이그레이션 시작';
        
        -- 기존 media 테이블이 있고 데이터가 있다면 마이그레이션
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'media') THEN
            -- url이 비어있는 레코드만 media 테이블에서 가져옴
            UPDATE place_media pm
            SET url = m.url,
                mime_type = m.mime_type
            FROM media m
            WHERE pm.media_id = m.id
            AND (pm.url IS NULL OR pm.url = '');
            
            RAISE NOTICE 'place_media 데이터 마이그레이션 완료';
        END IF;
        
        -- media_id 컬럼 삭제
        ALTER TABLE place_media DROP COLUMN media_id;
        RAISE NOTICE 'place_media.media_id 컬럼 삭제 완료';
    ELSE
        RAISE NOTICE 'place_media.media_id 컬럼 없음 - 스킵';
    END IF;
END $$;

-- ===================================================================
-- PART 2: content_media 데이터 마이그레이션
-- ===================================================================

-- 2-1. media_id 컬럼이 존재하면 데이터 마이그레이션 후 삭제
DO $$
BEGIN
    -- media_id 컬럼 존재 여부 확인
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'content_media' 
        AND column_name = 'media_id'
    ) THEN
        RAISE NOTICE 'content_media.media_id 컬럼 발견 - 데이터 마이그레이션 시작';
        
        -- 기존 media 테이블이 있고 데이터가 있다면 마이그레이션
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'media') THEN
            -- url이 비어있는 레코드만 media 테이블에서 가져옴
            UPDATE content_media cm
            SET url = m.url,
                mime_type = m.mime_type
            FROM media m
            WHERE cm.media_id = m.id
            AND (cm.url IS NULL OR cm.url = '');
            
            RAISE NOTICE 'content_media 데이터 마이그레이션 완료';
        END IF;
        
        -- media_id 컬럼 삭제
        ALTER TABLE content_media DROP COLUMN media_id;
        RAISE NOTICE 'content_media.media_id 컬럼 삭제 완료';
    ELSE
        RAISE NOTICE 'content_media.media_id 컬럼 없음 - 스킵';
    END IF;
END $$;

-- ===================================================================
-- PART 3: 구 Media 테이블 삭제
-- ===================================================================

-- 3-1. media 테이블 삭제 (CASCADE로 모든 의존 관계 함께 삭제)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'media') THEN
        DROP TABLE media CASCADE;
        RAISE NOTICE 'media 테이블 삭제 완료';
    ELSE
        RAISE NOTICE 'media 테이블 없음 - 스킵';
    END IF;
END $$;

-- ===================================================================
-- 마이그레이션 완료
-- ===================================================================

COMMENT ON TABLE place_media IS '장소 미디어 (직접 URL 저장)';
COMMENT ON TABLE content_media IS '콘텐츠 미디어 (직접 URL 저장)';
