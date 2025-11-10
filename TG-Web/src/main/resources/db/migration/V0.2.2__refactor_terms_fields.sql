-- ============================================================
-- 약관 필드 리팩토링
-- - required_agreed / terms_version 제거
-- - is_service_terms_and_privacy_agreed 통합 필드 추가
-- - marketing_agreed → is_marketing_agreed 이름 변경
-- - onboarding_step 추가
-- - 테이블이 없으면 아무 작업도 하지 않음
-- ============================================================

DO $$
BEGIN
  -------------------------------------------------------------------
  -- 0. member 테이블 존재 확인
  -------------------------------------------------------------------
  IF EXISTS (SELECT 1 FROM information_schema.tables
             WHERE table_schema = 'public' AND table_name = 'member') THEN

    -----------------------------------------------------------------
    -- 1. 새 필드 추가: is_service_terms_and_privacy_agreed
    -----------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                   AND table_name = 'member'
                   AND column_name = 'is_service_terms_and_privacy_agreed') THEN
ALTER TABLE public.member
    ADD COLUMN is_service_terms_and_privacy_agreed BOOLEAN DEFAULT NULL;
END IF;

    -----------------------------------------------------------------
    -- 2. 기존 회원 약관 동의 처리: null이면 true
    -----------------------------------------------------------------
UPDATE public.member
SET is_service_terms_and_privacy_agreed = true
WHERE is_service_terms_and_privacy_agreed IS NULL;

-----------------------------------------------------------------
-- 3. 기존 필드 제거: required_agreed, terms_version 등
-----------------------------------------------------------------
IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'member'
               AND column_name = 'required_agreed') THEN
ALTER TABLE public.member DROP COLUMN required_agreed;
END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'member'
               AND column_name = 'terms_version') THEN
ALTER TABLE public.member DROP COLUMN terms_version;
END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'member'
               AND column_name = 'is_service_terms_agreed') THEN
ALTER TABLE public.member DROP COLUMN is_service_terms_agreed;
END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'member'
               AND column_name = 'is_privacy_agreed') THEN
ALTER TABLE public.member DROP COLUMN is_privacy_agreed;
END IF;

    -----------------------------------------------------------------
    -- 4. marketingAgreed → is_marketing_agreed (이름 변경 또는 추가)
    -----------------------------------------------------------------
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public'
               AND table_name = 'member'
               AND column_name = 'marketing_agreed') THEN
ALTER TABLE public.member RENAME COLUMN marketing_agreed TO is_marketing_agreed;
ELSIF NOT EXISTS (SELECT 1 FROM information_schema.columns
                      WHERE table_schema = 'public'
                      AND table_name = 'member'
                      AND column_name = 'is_marketing_agreed') THEN
ALTER TABLE public.member ADD COLUMN is_marketing_agreed BOOLEAN DEFAULT false;
END IF;

    -----------------------------------------------------------------
    -- 5. onboarding_step 컬럼 추가
    -----------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public'
                   AND table_name = 'member'
                   AND column_name = 'onboarding_step') THEN
ALTER TABLE public.member ADD COLUMN onboarding_step VARCHAR(20);
END IF;

  -------------------------------------------------------------------
  -- 테이블이 존재하지 않으면 아무 것도 하지 않음
  -------------------------------------------------------------------
ELSE
    RAISE NOTICE 'Table "public.member" does not exist. Skipping migration.';
END IF;

END $$;
