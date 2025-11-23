package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.PlaceDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "장소", description = "장소 조회 API")
public interface PlaceControllerDocs {

  @Operation(
      summary = "장소 세부정보 조회",
      description = "장소 ID로 장소 상세 정보(Google Place ID 포함), 영업시간, 미디어를 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
  })
  ResponseEntity<PlaceDetailDto> getPlaceDetail(
      CustomUserDetails userDetails,
      @Parameter(description = "장소 ID", required = true) UUID placeId
  );
}
