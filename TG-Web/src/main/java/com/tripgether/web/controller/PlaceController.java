package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.PlaceDetailDto;
import com.tripgether.place.service.PlaceService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/place")
public class PlaceController implements PlaceControllerDocs {

  private final PlaceService placeService;

  /**
   * 장소 세부정보 조회
   * - 장소 기본 정보
   * - Google Place ID 등 플랫폼 참조 정보
   * - 영업시간
   * - 추가 미디어
   */
  @GetMapping("/{placeId}")
  @Override
  public ResponseEntity<PlaceDetailDto> getPlaceDetail(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID placeId
  ) {
    PlaceDetailDto response = placeService.getPlaceDetail(placeId);
    return ResponseEntity.ok(response);
  }
}
