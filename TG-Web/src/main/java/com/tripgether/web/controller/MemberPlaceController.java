package com.tripgether.web.controller;

import com.tripgether.auth.dto.CustomUserDetails;
import com.tripgether.place.dto.GetSavedPlacesResponse;
import com.tripgether.place.dto.GetTemporaryPlacesResponse;
import com.tripgether.place.dto.SavePlaceResponse;
import com.tripgether.place.service.MemberPlaceService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/member-place")
public class MemberPlaceController implements MemberPlaceControllerDocs {

  private final MemberPlaceService memberPlaceService;

  /**
   * 임시 저장 장소 목록 조회
   * - AI 분석으로 자동 생성된 장소들
   * - 사용자가 아직 저장 여부를 결정하지 않은 상태
   */
  @GetMapping("/temporary")
  @Override
  public ResponseEntity<GetTemporaryPlacesResponse> getTemporaryPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("Get temporary places request from member: {}", userDetails.getMemberId());

    GetTemporaryPlacesResponse response = memberPlaceService.getTemporaryPlaces(
        userDetails.getMemberId()
    );

    return ResponseEntity.ok(response);
  }

  /**
   * 저장한 장소 목록 조회
   * - 사용자가 명시적으로 저장한 장소들
   */
  @GetMapping("/saved")
  @Override
  public ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("Get saved places request from member: {}", userDetails.getMemberId());

    GetSavedPlacesResponse response = memberPlaceService.getSavedPlaces(
        userDetails.getMemberId()
    );

    return ResponseEntity.ok(response);
  }

  /**
   * 장소 저장
   * - 임시 저장 상태(TEMPORARY)에서 저장 상태(SAVED)로 변경
   */
  @PostMapping("/{placeId}/save")
  @Override
  public ResponseEntity<SavePlaceResponse> savePlace(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID placeId
  ) {
    log.info("Save place request from member: {}, placeId: {}",
        userDetails.getMemberId(), placeId);

    SavePlaceResponse response = memberPlaceService.savePlace(
        userDetails.getMemberId(),
        placeId
    );

    return ResponseEntity.ok(response);
  }

  /**
   * 임시 저장 장소 삭제
   * - TEMPORARY 상태의 장소만 삭제 가능
   * - SAVED 상태는 삭제 불가
   */
  @DeleteMapping("/{placeId}/temporary")
  @Override
  public ResponseEntity<Void> deleteTemporaryPlace(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID placeId
  ) {
    log.info("Delete temporary place request from member: {}, placeId: {}",
        userDetails.getMemberId(), placeId);

    memberPlaceService.deleteTemporaryPlace(
        userDetails.getMemberId(),
        placeId
    );

    return ResponseEntity.noContent().build();
  }
}
