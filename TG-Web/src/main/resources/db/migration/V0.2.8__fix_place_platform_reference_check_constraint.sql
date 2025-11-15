-- ============================================================
-- PlacePlatformReference 체크 제약 조건 수정
-- - 기존 제약 조건 삭제: place_external_platform_source_check
-- - 새로운 제약 조건 생성: place_platform_reference_platform_check
-- - 허용 값: NAVER, GOOGLE, KAKAO (대문자)
-- - 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. place_platform_reference 테이블 존재 확인
        -------------------------------------------------------------------
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'place_platform_reference') THEN
            -----------------------------------------------------------------
            -- 1. 기존 제약 조건 삭제 (존재하는 경우)
            -----------------------------------------------------------------
            IF EXISTS (SELECT 1
                       FROM information_schema.table_constraints
                       WHERE constraint_schema = 'public'
                         AND constraint_name = 'place_external_platform_source_check'
                         AND table_name = 'place_platform_reference') THEN
                ALTER TABLE public.place_platform_reference
                    DROP CONSTRAINT place_external_platform_source_check;
                RAISE NOTICE 'Dropped old constraint: place_external_platform_source_check';
            END IF;

            -----------------------------------------------------------------
            -- 2. 새로운 제약 조건 생성 (이미 존재하지 않는 경우)
            -----------------------------------------------------------------
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.table_constraints
                           WHERE constraint_schema = 'public'
                             AND constraint_name = 'place_platform_reference_platform_check'
                             AND table_name = 'place_platform_reference') THEN
                ALTER TABLE public.place_platform_reference
                    ADD CONSTRAINT place_platform_reference_platform_check
                    CHECK (place_platform IN ('NAVER', 'GOOGLE', 'KAKAO'));
                RAISE NOTICE 'Created new constraint: place_platform_reference_platform_check';
            ELSE
                RAISE NOTICE 'Constraint "place_platform_reference_platform_check" already exists. Skipping.';
            END IF;
        ELSE
            RAISE NOTICE 'Table "public.place_platform_reference" does not exist. Skipping migration. JPA will create the table automatically.';
        END IF;

    END
$$;

