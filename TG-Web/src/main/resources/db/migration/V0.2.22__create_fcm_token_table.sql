-- ============================================================
-- FCM Token 테이블 생성
-- - 멀티 디바이스 푸시 알림 지원
-- - Member와 1:N 관계
-- - 복합 유니크 제약: (member_id, device_id)
--
-- ⚠️ 중요: member 테이블 존재 확인 필수 (FK 제약조건 때문에)
-- - member 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- - fcm_token 테이블이 이미 있으면 아무 작업도 하지 않음
-- ============================================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. 부모 테이블(member) 존재 확인 - FK 제약조건 때문에 필수!
        -------------------------------------------------------------------
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'member') THEN

            -------------------------------------------------------------------
            -- 1. fcm_token 테이블 존재 확인
            -------------------------------------------------------------------
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.tables
                           WHERE table_schema = 'public'
                             AND table_name = 'fcm_token') THEN

                -----------------------------------------------------------------
                -- 2. fcm_token 테이블 생성
                -----------------------------------------------------------------
                CREATE TABLE public.fcm_token
                (
                    id             UUID                     NOT NULL,
                    member_id      UUID                     NOT NULL,
                    fcm_token      VARCHAR(500)             NOT NULL,
                    device_type    VARCHAR(10)              NOT NULL,
                    device_id      VARCHAR(100)             NOT NULL,
                    last_used_at   TIMESTAMP,
                    created_at     TIMESTAMP                NOT NULL,
                    updated_at     TIMESTAMP                NOT NULL,
                    created_by     VARCHAR(255),
                    updated_by     VARCHAR(255),
                    CONSTRAINT pk_fcm_token PRIMARY KEY (id),
                    CONSTRAINT fk_fcm_token_member FOREIGN KEY (member_id)
                        REFERENCES public.member (id) ON DELETE CASCADE
                );

                RAISE NOTICE 'Created table: fcm_token';

                -----------------------------------------------------------------
                -- 3. 유니크 제약 조건 생성
                -----------------------------------------------------------------
                ALTER TABLE public.fcm_token
                    ADD CONSTRAINT uk_member_device UNIQUE (member_id, device_id);

                RAISE NOTICE 'Created unique constraint: uk_member_device (member_id, device_id)';

                -----------------------------------------------------------------
                -- 4. 인덱스 생성
                -----------------------------------------------------------------
                CREATE INDEX idx_member_id ON public.fcm_token (member_id);
                CREATE INDEX idx_device_id ON public.fcm_token (device_id);

                RAISE NOTICE 'Created indexes: idx_member_id, idx_device_id';

                -----------------------------------------------------------------
                -- 5. CHECK 제약 조건 생성 (DeviceType Enum)
                -----------------------------------------------------------------
                ALTER TABLE public.fcm_token
                    ADD CONSTRAINT fcm_token_device_type_check
                        CHECK (device_type IN ('IOS', 'ANDROID'));

                RAISE NOTICE 'Created check constraint: fcm_token_device_type_check';

            ELSE
                RAISE NOTICE 'Table "public.fcm_token" already exists. Skipping migration. JPA will manage the table.';
            END IF;

        ELSE
            RAISE NOTICE 'Parent table "public.member" does not exist. Skipping fcm_token creation. JPA will create both tables automatically.';
        END IF;

    END
$$;
