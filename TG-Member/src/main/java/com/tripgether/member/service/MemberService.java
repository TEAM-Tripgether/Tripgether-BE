package com.tripgether.member.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.ErrorCodeBuilder;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Subject;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.BusinessStatus;
import com.tripgether.member.constant.MemberOnboardingStatus;
import com.tripgether.member.dto.MemberDto;
import com.tripgether.member.entity.Member;
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

  @Transactional
  public void agreeTerms(MemberDto memberDto) {
    UUID memberId = memberDto.getId();

    //회원 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    //필수 약관 미동의면 에러
    if (!Boolean.TRUE.equals(memberDto.isRequiredAgreed())) {
      log.warn("[Onboarding] 필수 약관에 동의하지 않음 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_TERMS_REQUIRED_NOT_AGREED);
    }

    //중복(멱등성) 체크 — 이미 동일한 상태라면 그대로 통과
    boolean sameAsIs =
        member.isRequiredAgreed()
            && member.isMarketingAgreed() == memberDto.isMarketingAgreed()
            && ((member.getTermsVersion() == null && memberDto.getTermsVersion() == null)
            || (member.getTermsVersion() != null
            && member.getTermsVersion().equals(memberDto.getTermsVersion())))
            && member.getOnboardingStatus() == MemberOnboardingStatus.COMPLETED;

    if (sameAsIs) {
      log.info("[Onboarding] 약관 동의 멱등 처리 - memberId={}, version={}",
          memberId, member.getTermsVersion());
      return;
    }

    //동의 정보 반영
    member.setRequiredAgreed(true);
    member.setMarketingAgreed(memberDto.isMarketingAgreed());
    member.setTermsVersion(memberDto.getTermsVersion());

    //온보딩 완료로 상태 업데이트
    if (member.getOnboardingStatus() != MemberOnboardingStatus.COMPLETED) {
      member.setOnboardingStatus(MemberOnboardingStatus.COMPLETED);
    }

    log.info("[Onboarding] 약관 동의 완료 - memberId={}, requiredAgreed={}, marketingAgreed={}, version={}, status={}",
        memberId, true, memberDto.isMarketingAgreed(), member.getTermsVersion(), member.getOnboardingStatus());
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
