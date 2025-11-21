package com.tripgether.web.controller;

import com.tripgether.sns.dto.RequestPlaceExtractionRequest;
import com.tripgether.sns.dto.RequestPlaceExtractionResponse;
import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.common.constant.Author;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface ContentControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.21", author = Author.SUHSAECHAN, issueNumber = 88, description = "ContentController 리팩토링에 따른 DOCS 간소화"),
      @ApiChangeLog(date = "2025.11.02", author = Author.KANGJIYUN, issueNumber = 54, description = "콘텐츠 Docs 추가 및 리팩토링"),
      @ApiChangeLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 22, description = "온보딩 성별 설정 API 추가")
  })
  @Operation(summary = "SNS URL로 콘텐츠 생성 및 장소 추출 요청", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`snsUrl`**: SNS URL (Instagram, YouTube Shorts 등)

              ## 반환값
              - **`contentId`**: 생성된 콘텐츠 ID
              - **`status`**: 장소 추출 상태 (PENDING, ANALYZING, COMPLETED, FAILED, DELETED)

              ## 동작 방식
              - SNS URL을 받아 콘텐츠를 생성하고 AI 서버에 장소 추출을 요청합니다.
              - 초기 상태는 `PENDING`이며, AI 서버 처리 완료 시 Webhook으로 상태가 업데이트됩니다.
              """)
  ResponseEntity<RequestPlaceExtractionResponse> requestPlaceExtraction(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody RequestPlaceExtractionRequest request);

}
