package com.tripgether.web.controller;

import com.tripgether.common.constant.Author;
import com.tripgether.place.dto.PlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface PlaceControllerDocs {
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
  ResponseEntity<List<PlaceResponse>> getSavedPlaces(UUID memberId);
}
