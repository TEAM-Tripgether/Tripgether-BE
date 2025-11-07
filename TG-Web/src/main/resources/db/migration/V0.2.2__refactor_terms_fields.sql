-- 약관 필드 리팩토링: requiredAgreed 통합 및 termsVersion 제거

-- 1. 새 필드 추가 (없으면 추가, 있으면 스킵)
-- JPA가 자동으로 생성하지만, 안전장치로 명시적 추가
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'public'
                 AND table_name = 'member'
                 AND column_name = 'is_service_terms_and_privacy_agreed') THEN
    ALTER TABLE public.member ADD COLUMN is_service_terms_and_privacy_agreed BOOLEAN DEFAULT NULL;
  END IF;
END $$;

-- 2. 기존 회원 약관 동의 완료 처리 (null인 경우 true로 설정)
-- 기존 회원은 이미 서비스를 사용 중이므로 동의한 것으로 간주
UPDATE public.member
SET is_service_terms_and_privacy_agreed = true
WHERE is_service_terms_and_privacy_agreed IS NULL;

-- 4. 기존 필드 제거 (있으면 제거)
DO $$
BEGIN
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
  
  -- 기존 분리된 필드가 있으면 제거
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
END $$;

-- 5. marketingAgreed 컬럼명 변경 (is_marketing_agreed로, 없으면 추가)
DO $$
BEGIN
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
END $$;

-- 6. onboardingStep 컬럼 추가 (캐싱용)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                 WHERE table_schema = 'public' 
                 AND table_name = 'member' 
                 AND column_name = 'onboarding_step') THEN
    ALTER TABLE public.member ADD COLUMN onboarding_step VARCHAR(20);
  END IF;
END $$;

