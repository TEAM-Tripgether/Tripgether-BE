package com.tripgether.member.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.ErrorCodeBuilder;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Subject;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.BusinessStatus;
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
        Member entity =
                Member.builder()
                        .email(memberDto.getEmail())
                        .nickname(memberDto.getNickname())
                        .profileImageUrl(memberDto.getProfileImageUrl())
                        .build();

        Member savedEntity = memberRepository.save(entity);
        return MemberDto.entityToDto(savedEntity);
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
        Member entity =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> {
                                    ErrorCodeBuilder errorCode =
                                            ErrorCodeBuilder.businessStatus(
                                                    Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
                                    return new CustomException(errorCode);
                                });

        return MemberDto.entityToDto(entity);
    }

    /**
     * 이메일로 회원 조회
     *
     * @param email 이메일
     * @return 회원 데이터
     */
    public MemberDto getMemberByEmail(String email) {
        Member entity =
                memberRepository.findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    ErrorCodeBuilder errorCode =
                                            ErrorCodeBuilder.businessStatus(
                                                    Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND);
                                    return new CustomException(errorCode);
                                });

        return MemberDto.entityToDto(entity);
    }
}
