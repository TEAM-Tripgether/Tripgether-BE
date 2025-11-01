package com.tripgether.web.controller;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.common.constant.Author;
import io.swagger.v3.oas.annotations.Operation;
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
              - **`platform`**: 콘텐츠 플랫폼 (INSTAGRAM, YOUTUBE, TIKTOK 등)
              - **`status`**: 장소 추출 요청 상태 (PENDING, ANALYZING, COMPLETED, FAILED, DELETED)
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
              - status는 처음에 `PENDING` 상태로 생성됩니다.
              - 생성된 콘텐츠를 기반으로 AI 서버에 장소 추출을 요청합니다.
              - 동일 콘텐츠에 대해 중복 요청이 불가능합니다.
              - 장소 추출 요청이 성공적으로 접수되면 상태가 `ANALYZING`으로 변경됩니다.
              - 장소 추출 요청이 성공적으로 완료되면 상태가 `COMPLETED`으로 변경됩니다.
              
              ## 에러코드
              - **`CONTENT_NOT_FOUND`**: 해당 콘텐츠를 찾을 수 없습니다.
              - **`INVALID_URL_FORMAT`**: 잘못된 URL 형식입니다.
              - **`DUPLICATE_CONTENT_REQUEST`**: 동일한 콘텐츠에 대한 중복 요청입니다.
              """)
  ResponseEntity<RequestPlaceExtractionResponse> createContent(PlaceExtractionRequest request);

}
