package com.tripgether.web.controller;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.common.constant.Author;
import com.tripgether.place.dto.GetSavedPlacesResponse;
import com.tripgether.place.dto.PlaceDto;
import com.tripgether.sns.dto.GetRecentContentResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface ContentControllerDocs {
  @ApiChangeLogs(
      @ApiChangeLog(
          date = "2025.11.02",
          author = Author.KANGJIYUN,
          issueNumber = 54,
          description = "콘텐츠 Docs 추가 및 리팩토링")
  )
  @Operation(
      summary = "콘텐츠 생성 후 장소 추출 요청",
      description =
          """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터
              - **`originalUrl`**: 콘텐츠 url (Instagram 게시물, YouTube Shorts 등)
              
              ## 반환값 (RequestPlaceExtractionResponse)
              - **`contentId`**: 콘텐츠 UUID
              - **`memberId`**: 회원 UUID
              - **`status`**: 장소 추출 요청 상태 (PENDING, ANALYZING, COMPLETED, FAILED, DELETED)
              
              ## 콘텐츠 필드
              - **`platform`**: 콘텐츠 플랫폼 (INSTAGRAM, YOUTUBE, TIKTOK 등)
              - **`platformUploader`**: 콘텐츠 업로더 계정 이름
              - **`caption`**: 게시글 본문
              - **`thumbnailUrl`**: 썸네일 URL
              - **`originalUrl`**: 원본 URL
              - **`title`**: 콘텐츠 제목
              - **`summary`**: 콘텐츠 요약
              - **`lastCheckedAt`**: 콘텐츠 마지막 조회 시간
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 프론트엔드에서 전달한 SNS URL을 기반으로 콘텐츠를 생성합니다.
              - **동일 URL로 이미 COMPLETED된 콘텐츠가 있으면 AI 요청 없이 기존 데이터를 즉시 반환합니다.** (중복 방지 및 비용 절감)
              - PENDING/FAILED 상태의 콘텐츠는 재사용하여 AI 서버에 재요청합니다.
              - status는 처음에 `PENDING` 상태로 생성됩니다.
              - 생성된 콘텐츠를 기반으로 AI 서버에 장소 추출을 요청합니다.
              - 장소 추출 요청이 성공적으로 접수되면 상태가 `PENDING`으로 유지됩니다.
              - AI 서버 처리 완료 시 Webhook을 통해 `COMPLETED` 또는 `FAILED`로 변경됩니다.
              - URL은 최대 2048자까지 허용됩니다.

              ## 에러코드
              - **`CONTENT_NOT_FOUND`**: 해당 콘텐츠를 찾을 수 없습니다.
              - **`URL_TOO_LONG`**: URL이 허용된 최대 길이(2048자)를 초과했습니다.
              - **`AI_SERVER_ERROR`**: AI 서버 처리 중 오류가 발생했습니다.
              """)
  ResponseEntity<RequestPlaceExtractionResponse> createContentAndRequestPlaceExtraction(CustomUserDetails userDetails, PlaceExtractionRequest request);

  @ApiChangeLogs(
      @ApiChangeLog(
          date = "2025.11.16",
          author = Author.KANGJIYUN,
          issueNumber = 78,
          description = "최근 콘텐츠 조회 Docs 추가 및 리팩토링")
  )
  @Operation(
      summary = "최근 SNS 콘텐츠 목록 조회",
      description =
          """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터
              - JWT 인증만 필요, 별도 파라미터 없음

              ## 반환값 (List<RecentContentResponse>)
              - **`contentId`**: 콘텐츠 UUID
              - **`platform`**: 콘텐츠 플랫폼 (INSTAGRAM, YOUTUBE, TIKTOK 등)
              - **`title`**: 콘텐츠 제목
              - **`thumbnailUrl`**: 썸네일 URL
              - **`originalUrl`**: 원본 URL
              - **`status`**: 콘텐츠 상태 (PENDING, COMPLETED, FAILED 등)
              - **`createdAt`**: 생성일시

              ## 특이사항
              - 인증된 사용자의 최근 10개 SNS 콘텐츠 목록을 생성일시 내림차순으로 조회합니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 해당 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<GetRecentContentResponse> getRecentContents(CustomUserDetails userDetails);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.20", author = Author.KANGJIYUN, issueNumber = 80, description = "최신순으로 장소 조회")
  })
  @Operation(
      summary = "사용자 장소 조회",
      description =
          """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터
              - JWT 인증만 필요, 별도 파라미터 없음
              
              ## 반환값 (List<>)
              - **`placeId`**: 장소 ID
              - **`name`**: 장소 이름
              - **`address`**: 장소 주소
              - **`rating`**: 장소 평점
              - **`photoUrls`**: 장소 사진 URL 목록
              - **`description`**: 장소 설명
              
              ## 특이사항
              - 최신순으로 장소를 조회합니다.
              - 최대 10개의 장소를 반환합니다.
              - 장소의 사진 URL은 최대 10개까지 반환합니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(CustomUserDetails userDetails);

}
