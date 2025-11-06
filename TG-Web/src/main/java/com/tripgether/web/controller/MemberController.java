package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.member.constant.MemberOnboardingStatus;
import com.tripgether.member.dto.MemberDto;
import com.tripgether.member.dto.TermAgreementRequest;
import com.tripgether.member.dto.TermAgreementResponse;
import com.tripgether.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 관리", description = "회원 생성, 조회 등의 기능을 제공하는 API")
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
    MemberDto dto = memberService.createMember(memberDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dto);
  }

  @PostMapping("/terms")
  @Operation(summary = "약관 동의", description = "필수 약관 동의 시 회원의 온보딩 상태를 COMPLETED로 변경합니다.")
  public ResponseEntity<TermAgreementResponse> agreeMemberTerms(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody TermAgreementRequest request
  ) {
    log.info("[Onboarding] 약관 동의 요청 - memberId={}", user.getMemberId());

    boolean requiredAgreed =
        Boolean.TRUE.equals(request.getAgreedToTerms()) &&
            Boolean.TRUE.equals(request.getAgreedToPrivacy());

    MemberDto dto = MemberDto.builder()
        .id(user.getMemberId())
        .requiredAgreed(requiredAgreed)
        .marketingAgreed(Boolean.TRUE.equals(request.getAgreedToMarketing()))
        .termsVersion(request.getTermsVersion())
        .build();

    memberService.agreeTerms(dto);

    // 동의 후에는 무조건 COMPLETED
    TermAgreementResponse response = TermAgreementResponse.builder()
        .completed(true)
        .onboardingStatus(MemberOnboardingStatus.COMPLETED)
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<MemberDto>> getAllMembers() {
    List<MemberDto> dtos = memberService.getAllMembers();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<MemberDto> getMemberById(@PathVariable UUID memberId) {
    MemberDto dto = memberService.getMemberById(memberId);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/email/{email}")
  public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
    MemberDto dto = memberService.getMemberByEmail(email);
    return ResponseEntity.ok(dto);
  }
}
