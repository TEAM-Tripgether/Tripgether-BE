package com.tripgether.web.controller;

import com.tripgether.common.constant.Author;
import com.tripgether.member.dto.MemberDto;
import io.swagger.v3.oas.annotations.Operation;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface MemberControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API"
          + " 문서화")
  })
  @Operation(
      summary = "회원 생성",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터 (MemberDto)
              - **`email`**: 회원 이메일 (필수)
              - **`name`**: 회원 닉네임 (필수)
              - **`profileImageUrl`**: 프로필 이미지 URL (선택)
              - **`socialPlatform`**: 소셜 플랫폼 (KAKAO, GOOGLE)
              - **`memberRole`**: 회원 권한 (ROLE_USER, ROLE_ADMIN)
              - **`status`**: 회원 상태 (ACTIVE, INACTIVE, DELETED)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 생성된 회원 ID
              - **`email`**: 회원 이메일
              - **`name`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 새로운 회원을 생성합니다.
              - 이메일 중복 검사가 수행됩니다.
              
              ## 에러코드
              - **`EMAIL_ALREADY_EXISTS`**: 이미 가입된 이메일입니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> createMember(MemberDto memberDto);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API"
          + " 문서화")
  })
  @Operation(
      summary = "전체 회원 목록 조회",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - 없음
              
              ## 반환값 (List<MemberDto>)
              - 전체 회원 목록을 반환합니다.
              - 각 회원의 상세 정보가 포함됩니다.
              
              ## 특이사항
              - 모든 회원 데이터를 조회합니다.
              - 삭제되지 않은 회원만 조회됩니다.
              
              ## 에러코드
              - **`INTERNAL_SERVER_ERROR`**: 서버에 문제가 발생했습니다.
              """)
  ResponseEntity<List<MemberDto>> getAllMembers();

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API"
          + " 문서화")
  })
  @Operation(
      summary = "회원 단건 조회 (ID)",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - **`memberId`**: 회원 ID (Path Variable)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 회원 ID
              - **`email`**: 회원 이메일
              - **`nickname`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 회원 ID로 특정 회원을 조회합니다.
              - 삭제된 회원은 조회되지 않습니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> getMemberById(UUID memberId);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API"
          + " 문서화")
  })
  @Operation(
      summary = "회원 단건 조회 (Email)",
      description =
          """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - **`email`**: 회원 이메일 (Path Variable)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 회원 ID
              - **`email`**: 회원 이메일
              - **`nickname`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫": 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 이메일로 특정 회원을 조회합니다.
              - 삭제된 회원은 조회되지 않습니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> getMemberByEmail(String email);
}
