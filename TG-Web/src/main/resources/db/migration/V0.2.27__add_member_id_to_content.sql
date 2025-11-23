-- ============================================================
-- Content 테이블에 member_id 컬럼 추가
-- - Member가 소유한 Content 조회를 위한 필드
-- - Member와 N:1 관계
-- - 인덱스 생성으로 조회 성능 최적화
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 1. member_id 컬럼이 없으면 추가
        -------------------------------------------------------------------
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'content'
                         AND column_name = 'member_id') THEN

            ALTER TABLE public.content
                ADD COLUMN member_id UUID;

            RAISE NOTICE 'Added column: member_id to content table';

            -----------------------------------------------------------------
            -- 2. 외래 키 제약 조건 추가
            -----------------------------------------------------------------
            ALTER TABLE public.content
                ADD CONSTRAINT fk_content_member FOREIGN KEY (member_id)
                    REFERENCES public.member (id) ON DELETE SET NULL;

            RAISE NOTICE 'Created foreign key constraint: fk_content_member';

            -----------------------------------------------------------------
            -- 3. 인덱스 생성 (조회 성능 최적화)
            -----------------------------------------------------------------
            CREATE INDEX idx_content_member_id ON public.content (member_id);
            CREATE INDEX idx_content_created_at ON public.content (created_at);
            CREATE INDEX idx_content_member_created ON public.content (member_id, created_at DESC);

            RAISE NOTICE 'Created indexes: idx_content_member_id, idx_content_created_at, idx_content_member_created';

        ELSE
            RAISE NOTICE 'Column "member_id" already exists in "public.content". Skipping migration.';
        END IF;

    END
$$;
