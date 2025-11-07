package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.member.dto.MemberDto;
import com.tripgether.member.dto.UpdateServiceAgreementTermsRequest;
import com.tripgether.member.dto.UpdateServiceAgreementTermsResponse;
import com.tripgether.member.dto.onboarding.response.OnboardingResponse;
import com.tripgether.member.dto.onboarding.request.UpdateBirthDateRequest;
import com.tripgether.member.dto.onboarding.request.UpdateGenderRequest;
import com.tripgether.member.dto.onboarding.request.UpdateInterestsRequest;
import com.tripgether.member.dto.onboarding.request.UpdateNameRequest;
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
  @Operation(summary = "회원 생성")
  public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
    MemberDto dto = memberService.createMember(memberDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dto);
  }

  @PostMapping("/onboarding/terms")
  @Operation(summary = "약관 동의")
  public ResponseEntity<UpdateServiceAgreementTermsResponse> agreeMemberTerms(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateServiceAgreementTermsRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    UpdateServiceAgreementTermsResponse response = memberService.agreeTerms(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/name")
  @Operation(summary = "이름 설정")
  public ResponseEntity<OnboardingResponse> updateName(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateNameRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateName(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/birth-date")
  @Operation(summary = "생년월일 설정")
  public ResponseEntity<OnboardingResponse> updateBirthDate(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateBirthDateRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateBirthDate(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/gender")
  @Operation(summary = "성별 설정")
  public ResponseEntity<OnboardingResponse> updateGender(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateGenderRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateGender(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/interests")
  @Operation(summary = "관심사 설정")
  public ResponseEntity<OnboardingResponse> updateInterests(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateInterestsRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateInterests(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "전체 회원 목록 조회")
  public ResponseEntity<List<MemberDto>> getAllMembers() {
    List<MemberDto> dtos = memberService.getAllMembers();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{memberId}")
  @Operation(summary = "회원 단건 조회 (ID)")
  public ResponseEntity<MemberDto> getMemberById(@PathVariable UUID memberId) {
    MemberDto dto = memberService.getMemberById(memberId);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/email/{email}")
  @Operation(summary = "회원 단건 조회 (Email)")
  public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
    MemberDto dto = memberService.getMemberByEmail(email);
    return ResponseEntity.ok(dto);
  }
}
