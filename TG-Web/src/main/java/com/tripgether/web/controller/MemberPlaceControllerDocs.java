package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.GetSavedPlacesResponse;
import com.tripgether.place.dto.GetTemporaryPlacesResponse;
import com.tripgether.place.dto.SavePlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "회원 장소", description = "회원별 장소 관리 API")
public interface MemberPlaceControllerDocs {

  @Operation(
      summary = "임시 저장 장소 목록 조회",
      description = "AI 분석으로 자동 생성된 임시 저장 장소 목록을 조회합니다. 사용자가 아직 저장 여부를 결정하지 않은 상태입니다."
  )
  ResponseEntity<GetTemporaryPlacesResponse> getTemporaryPlaces(
      CustomUserDetails userDetails
  );

  @Operation(
      summary = "저장한 장소 목록 조회",
      description = "사용자가 명시적으로 저장한 장소 목록을 조회합니다."
  )
  ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(
      CustomUserDetails userDetails
  );

  @Operation(
      summary = "장소 저장",
      description = "임시 저장 상태(TEMPORARY)의 장소를 저장 상태(SAVED)로 변경합니다."
  )
  ResponseEntity<SavePlaceResponse> savePlace(
      CustomUserDetails userDetails,
      @Parameter(description = "저장할 장소 ID", required = true) UUID placeId
  );

  @Operation(
      summary = "임시 저장 장소 삭제",
      description = "임시 저장 상태(TEMPORARY)의 장소를 삭제합니다. 저장된 상태(SAVED)의 장소는 삭제할 수 없습니다."
  )
  ResponseEntity<Void> deleteTemporaryPlace(
      CustomUserDetails userDetails,
      @Parameter(description = "삭제할 장소 ID", required = true) UUID placeId
  );
}
