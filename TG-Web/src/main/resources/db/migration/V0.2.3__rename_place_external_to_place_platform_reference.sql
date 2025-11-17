-- ============================================================
-- PlaceExternal → PlacePlatformReference 리팩토링
-- - 테이블명 변경: place_external → place_platform_reference
-- - 컬럼명 변경: external_id → place_platform_id
-- - 컬럼명 변경: platform_source → place_platform
-- - 컬럼 길이 변경: place_platform_id VARCHAR(255) → VARCHAR(300)
-- - Enum 값 변경: Naver → NAVER, Google → GOOGLE, Kakao → KAKAO
-- - 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- - 중복 실행 방지: place_platform_reference 테이블이 이미 존재하면 스킵
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. place_external 테이블 존재 확인
        -------------------------------------------------------------------
        IF
            EXISTS (SELECT 1
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_name = 'place_external') THEN
            -----------------------------------------------------------------
            -- 1. place_platform_reference 테이블이 이미 존재하는지 확인 (중복 방지)
            -----------------------------------------------------------------
            IF
                NOT EXISTS (SELECT 1
                           FROM information_schema.tables
                           WHERE table_schema = 'public'
                             AND table_name = 'place_platform_reference') THEN
                -----------------------------------------------------------------
                -- 2. 테이블명 변경: place_external → place_platform_reference
                -----------------------------------------------------------------
                ALTER TABLE public.place_external
                    RENAME TO place_platform_reference;

                -----------------------------------------------------------------
                -- 3. 컬럼명 변경: external_id → place_platform_id
                -----------------------------------------------------------------
                IF
                    EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'place_platform_reference'
                              AND column_name = 'external_id') THEN
                    ALTER TABLE public.place_platform_reference
                        RENAME COLUMN external_id TO place_platform_id;
                END IF;

                -----------------------------------------------------------------
                -- 4. 컬럼 길이 변경: place_platform_id VARCHAR(255) → VARCHAR(300)
                -----------------------------------------------------------------
                IF
                    EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'place_platform_reference'
                              AND column_name = 'place_platform_id'
                              AND character_maximum_length = 255) THEN
                    ALTER TABLE public.place_platform_reference
                        ALTER COLUMN place_platform_id TYPE VARCHAR(300);
                END IF;

                -----------------------------------------------------------------
                -- 5. 컬럼명 변경: platform_source → place_platform
                -----------------------------------------------------------------
                IF
                    EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'place_platform_reference'
                              AND column_name = 'platform_source') THEN
                    ALTER TABLE public.place_platform_reference
                        RENAME COLUMN platform_source TO place_platform;
                END IF;

                -----------------------------------------------------------------
                -- 6. Enum 값 변경: Naver → NAVER
                -----------------------------------------------------------------
                IF
                    EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'place_platform_reference'
                              AND column_name = 'place_platform') THEN
                    UPDATE public.place_platform_reference
                    SET place_platform = 'NAVER'
                    WHERE place_platform = 'Naver';
                END IF;

                -----------------------------------------------------------------
                -- 7. Enum 값 변경: Google → GOOGLE
                -----------------------------------------------------------------
                IF
                    EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'place_platform_reference'
                              AND column_name = 'place_platform') THEN
                    UPDATE public.place_platform_reference
                    SET place_platform = 'GOOGLE'
                    WHERE place_platform = 'Google';
                END IF;
            ELSE
                RAISE NOTICE 'Table "public.place_platform_reference" already exists. Skipping migration.';
            END IF;
            -------------------------------------------------------------------
            -- 테이블이 존재하지 않으면 아무 것도 하지 않음 (JPA가 자동 생성)
            -------------------------------------------------------------------
        ELSE
            RAISE NOTICE 'Table "public.place_external" does not exist. Skipping migration. JPA will create the table automatically.';
        END IF;

    END
$$;
