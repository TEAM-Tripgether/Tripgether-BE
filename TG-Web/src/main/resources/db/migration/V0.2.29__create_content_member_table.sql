-- =====================================================
-- V0.2.29: Create ContentMember table for N:M relationship
-- =====================================================
-- Description:
--   Content와 Member 간의 N:M 관계를 위한 중간 테이블 생성
--   여러 회원이 같은 Content를 공유할 수 있도록 변경
--   기존 Content.member_id 데이터를 ContentMember로 마이그레이션
--
-- ⚠️ 중요: content, member 테이블 존재 확인 필수 (FK 제약조건 때문에)
-- - 부모 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- =====================================================

-- Step 1: ContentMember 테이블 생성 (부모 테이블 존재 확인 후)
DO $$
BEGIN
    -- 부모 테이블 존재 확인: content, member 모두 있어야 함
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'content')
       AND EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'public' AND table_name = 'member') THEN

        -- content_member 테이블 존재 확인
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                       WHERE table_schema = 'public'
                       AND table_name = 'content_member') THEN

            CREATE TABLE content_member (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                content_id UUID NOT NULL,
                member_id UUID NOT NULL,
                notified BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                -- Foreign Keys
                CONSTRAINT fk_content_member_content
                    FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
                CONSTRAINT fk_content_member_member
                    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,

                -- Unique Constraint: 동일한 Content-Member 조합은 한 번만
                CONSTRAINT uk_content_member_content_member
                    UNIQUE (content_id, member_id)
            );

            -- Indexes for query performance
            CREATE INDEX idx_content_member_content_id ON content_member(content_id);
            CREATE INDEX idx_content_member_member_id ON content_member(member_id);
            CREATE INDEX idx_content_member_notified ON content_member(notified);

            RAISE NOTICE 'ContentMember table created successfully';
        ELSE
            RAISE NOTICE 'ContentMember table already exists, skipping creation';
        END IF;
    ELSE
        RAISE NOTICE 'Parent tables (content, member) do not exist. Skipping content_member creation. JPA will create all tables automatically.';
    END IF;
END $$;

-- Step 2: 기존 Content.member_id 데이터를 ContentMember로 마이그레이션
-- (Content 테이블에 member_id 컬럼이 존재하는 경우에만 실행)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'content'
               AND column_name = 'member_id') THEN

        -- member_id가 NULL이 아닌 Content에 대해 ContentMember 생성
        INSERT INTO content_member (id, content_id, member_id, notified, created_at, updated_at)
        SELECT
            gen_random_uuid(),
            c.id,
            c.member_id,
            TRUE,  -- 기존 데이터는 이미 처리된 것으로 간주 (알림 불필요)
            c.created_at,
            c.updated_at
        FROM content c
        WHERE c.member_id IS NOT NULL
        ON CONFLICT (content_id, member_id) DO NOTHING;

        RAISE NOTICE 'Migrated existing Content.member_id data to ContentMember';
    ELSE
        RAISE NOTICE 'Content.member_id column does not exist, skipping data migration';
    END IF;
END $$;

-- Step 3: Content 테이블에서 member_id 컬럼 제거
-- (컬럼이 존재하는 경우에만 실행)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'content'
               AND column_name = 'member_id') THEN

        -- Foreign Key 제약 조건 먼저 제거
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_schema = 'public'
                   AND table_name = 'content'
                   AND constraint_name = 'fk_content_member') THEN
            ALTER TABLE content DROP CONSTRAINT fk_content_member;
            RAISE NOTICE 'Dropped FK constraint fk_content_member';
        END IF;

        -- Index 제거 (존재하는 경우)
        IF EXISTS (SELECT 1 FROM pg_indexes
                   WHERE schemaname = 'public'
                   AND tablename = 'content'
                   AND indexname = 'idx_content_member_id') THEN
            DROP INDEX idx_content_member_id;
            RAISE NOTICE 'Dropped index idx_content_member_id';
        END IF;

        -- member_id 컬럼 제거
        ALTER TABLE content DROP COLUMN member_id;
        RAISE NOTICE 'Dropped member_id column from Content table';
    ELSE
        RAISE NOTICE 'Content.member_id column does not exist, skipping column removal';
    END IF;
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Content와 Member는 이제 ContentMember를 통한 N:M 관계
-- 여러 회원이 같은 SNS URL의 Content를 공유 가능
-- 각 회원별로 알림 전송 상태(notified) 관리
-- =====================================================
