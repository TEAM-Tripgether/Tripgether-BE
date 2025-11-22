-- ============================================================
-- Content-Member M:N 관계 구현
-- - ContentMember 중간 테이블 생성
-- - Content의 member_id 데이터를 ContentMember로 마이그레이션
-- - Content에서 member_id 컬럼 삭제
-- - 여러 회원이 같은 Content를 공유하는 구조로 변경
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. content_member 테이블 존재 확인
        -------------------------------------------------------------------
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_name = 'content_member') THEN

            -----------------------------------------------------------------
            -- 1. content_member 테이블 생성
            -----------------------------------------------------------------
            CREATE TABLE public.content_member
            (
                id             UUID                     NOT NULL,
                content_id     UUID,
                member_id      UUID,
                notified       BOOLEAN                  NOT NULL DEFAULT FALSE,
                created_at     TIMESTAMP                NOT NULL,
                updated_at     TIMESTAMP                NOT NULL,
                created_by     VARCHAR(255),
                updated_by     VARCHAR(255),
                CONSTRAINT pk_content_member PRIMARY KEY (id),
                CONSTRAINT fk_content_member_content FOREIGN KEY (content_id)
                    REFERENCES public.content (id) ON DELETE CASCADE,
                CONSTRAINT fk_content_member_member FOREIGN KEY (member_id)
                    REFERENCES public.member (id) ON DELETE CASCADE
            );

            RAISE NOTICE 'Created table: content_member';

            -----------------------------------------------------------------
            -- 2. 유니크 제약 조건 생성 (중복 방지)
            -----------------------------------------------------------------
            ALTER TABLE public.content_member
                ADD CONSTRAINT uk_content_member UNIQUE (content_id, member_id);

            RAISE NOTICE 'Created unique constraint: uk_content_member (content_id, member_id)';

            -----------------------------------------------------------------
            -- 3. 인덱스 생성
            -----------------------------------------------------------------
            CREATE INDEX idx_content_member_content_id ON public.content_member (content_id);
            CREATE INDEX idx_content_member_member_id ON public.content_member (member_id);
            CREATE INDEX idx_content_member_notified ON public.content_member (notified);

            RAISE NOTICE 'Created indexes: idx_content_member_content_id, idx_content_member_member_id, idx_content_member_notified';

            -----------------------------------------------------------------
            -- 4. 기존 Content의 member_id 데이터를 ContentMember로 마이그레이션
            -----------------------------------------------------------------
            IF EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'content'
                         AND column_name = 'member_id') THEN

                -- Content에 member_id가 있는 레코드만 ContentMember 생성
                INSERT INTO public.content_member (id, content_id, member_id, notified, created_at, updated_at)
                SELECT gen_random_uuid(),
                       c.id,
                       c.member_id,
                       CASE
                           WHEN c.status = 'COMPLETED' THEN TRUE
                           ELSE FALSE
                       END,
                       c.created_at,
                       NOW()
                FROM public.content c
                WHERE c.member_id IS NOT NULL;

                RAISE NOTICE 'Migrated existing Content.member_id data to ContentMember table';

                -------------------------------------------------------------------
                -- 5. Content 테이블에서 member_id 컬럼 삭제
                -------------------------------------------------------------------
                ALTER TABLE public.content DROP COLUMN member_id;

                RAISE NOTICE 'Dropped column: content.member_id';

            ELSE
                RAISE NOTICE 'Column "content.member_id" does not exist. Skipping data migration.';
            END IF;

        ELSE
            RAISE NOTICE 'Table "public.content_member" already exists. Skipping migration. JPA will manage the table.';
        END IF;

    END
$$;
