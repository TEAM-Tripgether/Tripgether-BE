package com.tripgether.web.controller;

import com.tripgether.member.dto.MemberDto;
import com.tripgether.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 관리", description = "회원 생성, 조회 등의 기능을 제공하는 API")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 생성",
            description = "새로운 회원을 생성합니다.")
    @PostMapping
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
        MemberDto dto = memberService.createMember(memberDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "전체 회원 목록 조회",
            description = "모든 회원 데이터를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> dtos = memberService.getAllMembers();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "회원 단건 조회 (ID)",
            description = "회원 ID로 특정 회원을 조회합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable UUID memberId) {
        MemberDto dto = memberService.getMemberById(memberId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "회원 단건 조회 (Email)",
            description = "이메일로 특정 회원을 조회합니다.")
    @GetMapping("/email/{email}")
    public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
        MemberDto dto = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(dto);
    }
}

