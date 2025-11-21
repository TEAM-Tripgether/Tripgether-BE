package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.PlaceResponse;
import com.tripgether.place.service.PlaceService;
import com.tripgether.sns.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "장소 관리", description = "장소 조회 등의 기능을 제공하는 API")
public class PlaceController {
  private final PlaceService placeService;

  /**
   * 사용자가 저장한 장소 목록 조회 (최신순, 최대 10개)
   */
  @GetMapping("/saved")
  @Operation(summary = "저장한 장소 목록 조회",
      description = "사용자가 저장한 장소 정보를 최신순으로 최대 10개까지 조회합니다.")
  public ResponseEntity<List<PlaceResponse>> getSavedPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<PlaceResponse> responses =
        placeService.getSavedPlaces(userDetails.getMemberId());
    return ResponseEntity.ok(responses);
  }
}
