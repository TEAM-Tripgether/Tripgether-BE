-- ============================================================
-- Place 테이블 name, address 컬럼 타입 수정
-- - name 컬럼: bytea → VARCHAR(255) (또는 text)
-- - address 컬럼: bytea → VARCHAR(500) (또는 text)
-- - TRIM/LOWER 함수 사용 가능하도록 문자열 타입으로 변경
-- - 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- - 컬럼이 이미 올바른 타입이면 스킵
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. place 테이블 존재 확인
        -------------------------------------------------------------------
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'place') THEN
            -----------------------------------------------------------------
            -- 1. name 컬럼 타입 변경: bytea → VARCHAR(255)
            -----------------------------------------------------------------
            IF EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'place'
                         AND column_name = 'name'
                         AND udt_name = 'bytea') THEN
                -- bytea → VARCHAR(255) 변환
                -- USING 절을 사용하여 UTF-8 인코딩된 바이너리 데이터를 텍스트로 변환
                ALTER TABLE public.place
                    ALTER COLUMN name TYPE VARCHAR(255) 
                    USING convert_from(name, 'UTF8');
                
                RAISE NOTICE 'Fixed place.name column type: bytea → VARCHAR(255)';
            ELSIF EXISTS (SELECT 1
                          FROM information_schema.columns
                          WHERE table_schema = 'public'
                            AND table_name = 'place'
                            AND column_name = 'name'
                            AND udt_name != 'bytea') THEN
                RAISE NOTICE 'place.name column is already correct type. Skipping.';
            END IF;

            -----------------------------------------------------------------
            -- 2. address 컬럼 타입 변경: bytea → VARCHAR(500)
            -----------------------------------------------------------------
            IF EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'place'
                         AND column_name = 'address'
                         AND udt_name = 'bytea') THEN
                -- bytea → VARCHAR(500) 변환
                -- USING 절을 사용하여 UTF-8 인코딩된 바이너리 데이터를 텍스트로 변환
                ALTER TABLE public.place
                    ALTER COLUMN address TYPE VARCHAR(500) 
                    USING convert_from(address, 'UTF8');
                
                RAISE NOTICE 'Fixed place.address column type: bytea → VARCHAR(500)';
            ELSIF EXISTS (SELECT 1
                          FROM information_schema.columns
                          WHERE table_schema = 'public'
                            AND table_name = 'place'
                            AND column_name = 'address'
                            AND udt_name != 'bytea') THEN
                RAISE NOTICE 'place.address column is already correct type. Skipping.';
            END IF;
        ELSE
            RAISE NOTICE 'Table "public.place" does not exist. Skipping migration. JPA will create the table automatically.';
        END IF;

    END
$$;

