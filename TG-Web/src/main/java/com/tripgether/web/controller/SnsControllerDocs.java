package com.tripgether.web.controller;

import com.tripgether.common.constant.Author;
import com.tripgether.sns.dto.InstagramApiRequest;
import com.tripgether.sns.dto.InstagramApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface SnsControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.11.05",
          author = Author.SUHSAECHAN,
          issueNumber = 0,
          description = "Instagram API 응답 캡처 API 구현 (테스트)")
  })
  @Operation(
      summary = "Instagram API 응답 캡처",
      description =
          """
              ## 인증: **불필요** (화이트리스트 적용)

              ## 요청 파라미터 (InstagramApiRequest)
              - **`instagramUrl`**: Instagram 콘텐츠 URL (필수)
                - 형식: `https://www.instagram.com/p/...` 또는 `https://www.instagram.com/reel/...`

              ## 반환값 (InstagramApiResponse)
              - **`apiResponse`**: sssinstagram.com으로부터 캡처한 전체 API 응답 (JsonArray)
              - **`elapsedTimeMs`**: API 응답 캡처 소요 시간 (밀리초)
              - **`instagramUrl`**: 요청한 Instagram URL

              ## 특이사항
              - 이 API는 테스트 목적으로 구현되었습니다.
              - Selenium을 사용하여 sssinstagram.com의 API 응답을 캡처합니다.
              - 로컬 환경에서는 ChromeDriver를 직접 사용합니다.
              - 서버 환경에서는 Selenium Grid에 연결하여 실행합니다.
              - 응답 시간은 일반적으로 2-5초 정도 소요됩니다.

              ## 에러코드
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 Instagram URL입니다.
              - **`SNS_API_REQUEST_FAILED`**: Instagram API 응답 캡처에 실패했습니다.
              """)
  ResponseEntity<InstagramApiResponse> captureInstagramApi(InstagramApiRequest request);
}
