package com.tripgether.member.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.ErrorCodeBuilder;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Subject;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.BusinessStatus;
import com.tripgether.member.constant.MemberGender;
import com.tripgether.member.constant.MemberOnboardingStatus;
import com.tripgether.member.constant.OnboardingStep;
import com.tripgether.member.dto.MemberDto;
import com.tripgether.member.dto.updateServiceAgreementTermsRequest;
import com.tripgether.member.dto.onboarding.request.UpdateBirthDateRequest;
import com.tripgether.member.dto.onboarding.request.UpdateGenderRequest;
import com.tripgether.member.dto.onboarding.request.UpdateInterestsRequest;
import com.tripgether.member.dto.onboarding.request.UpdateNameRequest;
import com.tripgether.member.entity.Interest;
import com.tripgether.member.entity.Member;
import com.tripgether.member.entity.MemberInterest;
import com.tripgether.member.repository.InterestRepository;
import com.tripgether.member.repository.MemberInterestRepository;
import com.tripgether.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberInterestRepository memberInterestRepository;
  private final InterestRepository interestRepository;

  /**
   * 회원 생성
   *
   * @param memberDto 생성할 회원 데이터
   * @return 생성된 회원 데이터
   */
  @Transactional
  public MemberDto createMember(MemberDto memberDto) {
    // 이메일 중복 체크
    if (memberRepository.existsByEmail(memberDto.getEmail())) {
      ErrorCodeBuilder errorCode =
          ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.DUPLICATE, HttpStatus.CONFLICT);
      throw new CustomException(errorCode);
    }

    // Entity 변환 및 저장
    Member entity = Member.builder()
        .email(memberDto.getEmail())
        .name(memberDto.getName())
        .build();

    Member savedEntity = memberRepository.save(entity);
    return MemberDto.entityToDto(savedEntity);
  }

  /**
   * 온보딩 단계 계산
   *
   * @param member 회원 엔티티
   * @return 현재 온보딩 단계
   */
  public OnboardingStep calculateOnboardingStep(Member member) {
    // COMPLETED 상태면 바로 반환 (쿼리 최적화)
    if (member.getOnboardingStatus() == MemberOnboardingStatus.COMPLETED) {
      return OnboardingStep.COMPLETED;
    }

    // 캐시된 onboardingStep이 있고 유효하면 반환
    if (member.getOnboardingStep() != null) {
      return member.getOnboardingStep();
    }

    // 약관 동의 체크
    if (!Boolean.TRUE.equals(member.getIsServiceTermsAndPrivacyAgreed())) {
      return OnboardingStep.TERMS;
    }

    // 이름 체크 (기본값이거나 빈 문자열인 경우)
    if (member.getName() == null || member.getName().trim().isEmpty()) {
      return OnboardingStep.NAME;
    }

    // 생년월일 체크
    if (member.getBirthDate() == null) {
      return OnboardingStep.BIRTH_DATE;
    }

    // 성별 체크
    if (member.getGender() == null) {
      return OnboardingStep.GENDER;
    }

    // 관심사 체크
    List<MemberInterest> interests = memberInterestRepository.findByMemberId(member.getId());
    if (interests == null || interests.isEmpty()) {
      return OnboardingStep.INTERESTS;
    }

    // 모든 단계 완료
    return OnboardingStep.COMPLETED;
  }

  /**
   * 온보딩 단계 계산 및 저장
   *
   * @param member 회원 엔티티
   * @return 계산된 온보딩 단계
   */
  @Transactional
  public OnboardingStep calculateAndSaveOnboardingStep(Member member) {
    OnboardingStep step = calculateOnboardingStep(member);
    member.setOnboardingStep(step);
    return step;
  }

  /**
   * 약관 동의 처리
   *
   * @param request 약관 동의 요청
   */
  @Transactional
  public void agreeTerms(updateServiceAgreementTermsRequest request) {
    UUID memberId = request.getMemberId();
    
    // 회원 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 필수 약관 검증
    if (!Boolean.TRUE.equals(request.getIsServiceTermsAndPrivacyAgreed())) {
      log.warn("[Onboarding] 필수 약관에 동의하지 않음 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_TERMS_REQUIRED_NOT_AGREED);
    }

    // 동의 정보 반영
    member.setIsServiceTermsAndPrivacyAgreed(true);
    member.setIsMarketingAgreed(Boolean.TRUE.equals(request.getIsMarketingAgreed()));

    // 온보딩 상태를 IN_PROGRESS로 변경 (COMPLETED로 변경하지 않음)
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 약관 동의 완료 - memberId={}, isServiceTermsAndPrivacyAgreed={}, isMarketingAgreed={}, status={}",
        memberId, true, member.getIsMarketingAgreed(), member.getOnboardingStatus());
  }

  /**
   * 이름 업데이트
   *
   * @param request 이름 업데이트 요청
   */
  @Transactional
  public void updateName(UpdateNameRequest request) {
    UUID memberId = request.getMemberId();
    
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    member.setName(request.getName());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 이름 업데이트 완료 - memberId={}, name={}", memberId, request.getName());
  }

  /**
   * 생년월일 업데이트
   *
   * @param request 생년월일 업데이트 요청
   */
  @Transactional
  public void updateBirthDate(UpdateBirthDateRequest request) {
    UUID memberId = request.getMemberId();
    
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    member.setBirthDate(request.getBirthDate());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 생년월일 업데이트 완료 - memberId={}, birthDate={}", memberId, request.getBirthDate());
  }

  /**
   * 성별 업데이트
   *
   * @param request 성별 업데이트 요청
   */
  @Transactional
  public void updateGender(UpdateGenderRequest request) {
    UUID memberId = request.getMemberId();
    
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    member.setGender(request.getGender());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 성별 업데이트 완료 - memberId={}, gender={}", memberId, request.getGender());
  }

  /**
   * 관심사 업데이트 (전체 교체)
   *
   * @param request 관심사 업데이트 요청
   */
  @Transactional
  public void updateInterests(UpdateInterestsRequest request) {
    UUID memberId = request.getMemberId();
    
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 관심사 ID 유효성 검증
    List<Interest> interests = interestRepository.findAllById(request.getInterestIds());
    if (interests.size() != request.getInterestIds().size()) {
      log.warn("[Onboarding] 유효하지 않은 관심사 ID 포함 - memberId={}", memberId);
      throw new CustomException(
          ErrorCodeBuilder.businessStatus(Subject.INTEREST, BusinessStatus.NOT_FOUND, HttpStatus.BAD_REQUEST));
    }

    // 기존 관심사 삭제
    memberInterestRepository.deleteByMemberId(memberId);

    // 새 관심사 추가
    List<MemberInterest> memberInterests = request.getInterestIds().stream()
        .map(interestId -> MemberInterest.builder()
            .member(member)
            .interest(interests.stream()
                .filter(i -> i.getId().equals(interestId))
                .findFirst()
                .orElseThrow(() -> new CustomException(
                    ErrorCodeBuilder.businessStatus(Subject.INTEREST, BusinessStatus.NOT_FOUND, HttpStatus.BAD_REQUEST))))
            .build())
        .collect(Collectors.toList());

    memberInterestRepository.saveAll(memberInterests);

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 관심사 업데이트 완료 - memberId={}, interestCount={}", memberId, memberInterests.size());
  }

  /**
   * 모든 회원 조회
   *
   * @return 회원 목록
   */
  public List<MemberDto> getAllMembers() {
    List<Member> entities = memberRepository.findAll();
    return entities.stream()
        .map(MemberDto::entityToDto)
        .collect(Collectors.toList());
  }

  /**
   * 회원 ID로 엔티티 조회 (내부용)
   *
   * @param memberId 회원 ID
   * @return 회원 엔티티
   */
  public Member getMemberEntityById(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)));
  }

  /**
   * 회원 ID로 조회
   *
   * @param memberId 회원 ID
   * @return 회원 데이터
   */
  public MemberDto getMemberById(UUID memberId) {
    Member entity = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)));

    return MemberDto.entityToDto(entity);
  }

  /**
   * 이메일로 회원 조회
   *
   * @param email 이메일
   * @return 회원 데이터
   */
  public MemberDto getMemberByEmail(String email) {
    Member entity = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)));

    return MemberDto.entityToDto(entity);
  }
}
