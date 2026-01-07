-- ============================================================
-- 고아 FCM Token 레코드 정리
-- - fcm_token, member 테이블이 존재할 때만 실행
-- - DB 전체 삭제 후 서버 시작 시에도 안전하게 동작
-- ============================================================
DO
$$
    BEGIN
        -- fcm_token 테이블 존재 확인
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'fcm_token') THEN

            -- member 테이블 존재 확인
            IF EXISTS (SELECT 1
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_name = 'member') THEN

                -- 1. member 테이블에 존재하지 않는 member_id를 가진 fcm_token 삭제
                DELETE FROM fcm_token ft
                WHERE NOT EXISTS (
                    SELECT 1 FROM member m
                    WHERE m.id = ft.member_id
                );

                -- 2. 소프트 삭제된 member의 fcm_token 삭제
                DELETE FROM fcm_token ft
                WHERE EXISTS (
                    SELECT 1 FROM member m
                    WHERE m.id = ft.member_id
                    AND m.deleted_at IS NOT NULL
                );

                RAISE NOTICE 'Cleaned up orphan FCM tokens successfully.';
            ELSE
                RAISE NOTICE 'Table "public.member" does not exist. Skipping migration. JPA will create the table.';
            END IF;
        ELSE
            RAISE NOTICE 'Table "public.fcm_token" does not exist. Skipping migration. JPA will create the table.';
        END IF;
    END
$$;
