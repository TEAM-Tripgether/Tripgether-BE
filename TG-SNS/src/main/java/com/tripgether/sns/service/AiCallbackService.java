package com.tripgether.sns.service;

import com.tripgether.ai.dto.AiCallbackRequest;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.place.entity.Place;
import com.tripgether.place.repository.PlaceRepository;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentPlace;
import com.tripgether.sns.repository.ContentPlaceRepository;
import com.tripgether.sns.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// AI 서버 Webhook Callback 처리
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCallbackService {

  private final ContentRepository contentRepository;
  private final PlaceRepository placeRepository;
  private final ContentPlaceRepository contentPlaceRepository;

  /**
   * AI 서버로부터 받은 Callback 처리
   *
   * - SUCCESS면 Place 저장
   * - FAILED면 상태만 변경
   *
   * @param request AI Callback 요청
   */
  @Transactional
  public void processAiServerCallback(AiCallbackRequest request) {
    log.info("Processing AI callback: contentId={}, resultStatus={}",
        request.getContentId(), request.getResultStatus());

    // Content 조회 - 없으면 예외 발생
    Content content = contentRepository.findById(request.getContentId())
        .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

    // 결과 상태에 따라 분기 처리
    if ("SUCCESS".equals(request.getResultStatus())) {
      // 성공 - Place 데이터 저장
      processAiServerSuccessCallback(content, request);
    } else if ("FAILED".equals(request.getResultStatus())) {
      // 실패 - Content 상태만 FAILED로 변경
      processFailedCallback(content, request);
    } else {
      // 알 수 없는 상태값 - 에러 처리
      log.error("Unknown resultStatus: {}", request.getResultStatus());
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    log.info("AI callback processed successfully: contentId={}", request.getContentId());
  }

  /**
   * 성공 Callback 처리
   *
   * - Content가 COMPLETED 상태: 기존 ContentPlace 삭제 후 재생성 (업데이트 모드)
   * - Content가 PENDING/FAILED 상태: 신규 ContentPlace 생성
   *
   * @param content 대상 Content
   * @param request AI Callback 요청
   */
  private void processAiServerSuccessCallback(Content content, AiCallbackRequest request) {
    log.debug("Processing SUCCESS callback for contentId={}", content.getId());

    // Content가 이미 COMPLETED 상태인지 확인 (재처리 요청 판단용)
    boolean isContentAlreadyCompleted = (content.getStatus() == ContentStatus.COMPLETED);

    if (isContentAlreadyCompleted) {
      // 업데이트 모드 - 기존 ContentPlace 모두 삭제
      log.info("Content already COMPLETED. Updating existing data: contentId={}", content.getId());
      contentPlaceRepository.deleteByContentId(content.getId());
      log.debug("Deleted existing ContentPlaces for contentId={}", content.getId());
    }

    // Content 상태를 COMPLETED로 변경 (신규 또는 재처리 모두)
    content.setStatus(ContentStatus.COMPLETED);
    contentRepository.save(content);

    // AI 서버에서 받은 Place 정보로 ContentPlace 생성
    if (request.getPlaces() != null && !request.getPlaces().isEmpty()) {
      List<AiCallbackRequest.PlaceInfo> places = request.getPlaces();
      log.info("Processing {} places for contentId={} (update mode: {})",
          places.size(), content.getId(), isContentAlreadyCompleted);

      // 각 Place 정보를 순회하며 저장
      for (int i = 0; i < places.size(); i++) {
        AiCallbackRequest.PlaceInfo placeInfo = places.get(i);

        // Place 조회 또는 생성 (이름+좌표로 중복 체크)
        Place place = findOrCreatePlace(placeInfo);

        // Content와 Place 연결 생성 (position 포함)
        createContentPlace(content, place, i);
      }
    } else {
      // Place 데이터가 없는 경우 경고 로그
      log.warn("No places found in callback for contentId={}", content.getId());
    }
  }

  /**
   * 실패 Callback 처리
   *
   * Content 상태를 FAILED로 변경
   *
   * @param content 대상 Content
   * @param request AI Callback 요청
   */
  private void processFailedCallback(Content content, AiCallbackRequest request) {
    log.error("Processing FAILED callback for contentId={}", content.getId());

    // Content 상태를 FAILED로 변경하고 저장
    content.setStatus(ContentStatus.FAILED);
    contentRepository.save(content);
  }

  /**
   * Place 조회 또는 생성
   * <p>
   * 이름+좌표로 중복 체크 후 없으면 신규 생성
   *
   * @param placeInfo Place 정보
   * @return 조회 또는 생성된 Place
   */
  private Place findOrCreatePlace(AiCallbackRequest.PlaceInfo placeInfo) {
    // 이름과 좌표가 동일한 Place 조회
    return placeRepository.findByNameAndLatitudeAndLongitude(
            placeInfo.getName(),
            placeInfo.getLatitude(),
            placeInfo.getLongitude())
        .orElseGet(() -> {
          // 기존 Place가 없으면 신규 생성
          Place newPlace = Place.builder()
              .name(placeInfo.getName())
              .address(placeInfo.getAddress())
              .country(placeInfo.getCountry())
              .latitude(placeInfo.getLatitude())
              .longitude(placeInfo.getLongitude())
              .description(placeInfo.getDescription())
              .build();

          // Place 저장 후 반환
          Place savedPlace = placeRepository.save(newPlace);
          log.debug("Created new place: id={}, name={}", savedPlace.getId(), savedPlace.getName());
          return savedPlace;
        });
  }

  /**
   * ContentPlace 연결 생성
   * <p>
   * Content와 Place 매핑 및 순서 저장
   *
   * @param content 대상 Content
   * @param place 대상 Place
   * @param position 순서
   */
  private void createContentPlace(Content content, Place place, int position) {
    // ContentPlace 엔티티 생성
    ContentPlace contentPlace = ContentPlace.builder()
        .content(content)
        .place(place)
        .position(position)  // Place 순서 정보
        .build();

    // ContentPlace 저장
    contentPlaceRepository.save(contentPlace);
    log.debug("Created ContentPlace: contentId={}, placeId={}, position={}", content.getId(), place.getId(), position);
  }
}
