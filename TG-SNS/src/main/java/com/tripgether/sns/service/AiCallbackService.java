package com.tripgether.sns.service;

import com.tripgether.ai.dto.AiCallbackRequest;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.place.entity.Place;
import com.tripgether.place.repository.PlaceRepository;
import com.tripgether.sns.constant.ContentStatus;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentPlace;
import com.tripgether.sns.repository.ContentPlaceRepository;
import com.tripgether.sns.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 서버 Webhook Callback 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCallbackService {

  private final ContentRepository contentRepository;
  private final PlaceRepository placeRepository;
  private final ContentPlaceRepository contentPlaceRepository;

  /**
   * AI 서버로부터 받은 Callback 데이터를 처리합니다.
   *
   * @param request Callback 요청 데이터
   */
  @Transactional
  public void processAiServerCallback(AiCallbackRequest request) {
    log.info("Processing AI callback: contentId={}, resultStatus={}",
        request.getContentId(), request.getResultStatus());

    // 1. Content 조회
    Content content = contentRepository.findById(request.getContentId())
        .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

    // 2. 결과 상태에 따른 처리
    if ("SUCCESS".equals(request.getResultStatus())) {
      processAiServerSuccessCallback(content, request);
    } else if ("FAILED".equals(request.getResultStatus())) {
      processFailedCallback(content, request);
    } else {
      log.error("Unknown resultStatus: {}", request.getResultStatus());
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    log.info("AI callback processed successfully: contentId={}", request.getContentId());
  }

  /**
   * 성공 Callback 처리
   */
  private void processAiServerSuccessCallback(Content content, AiCallbackRequest request) {
    log.debug("Processing SUCCESS callback for contentId={}", content.getId());

    // Content 상태를 COMPLETED로 변경
    content.updateStatus(ContentStatus.COMPLETED);
    contentRepository.save(content);

    // ContentInfo는 이미 Content에 있으므로, Place 데이터만 처리
    if (request.getPlaces() != null && !request.getPlaces().isEmpty()) {
      List<AiCallbackRequest.PlaceInfo> places = request.getPlaces();
      log.info("Processing {} places for contentId={}", places.size(), content.getId());

      for (int i = 0; i < places.size(); i++) {
        AiCallbackRequest.PlaceInfo placeInfo = places.get(i);

        // Place 생성 또는 조회 (중복 방지)
        Place place = findOrCreatePlace(placeInfo);

        // ContentPlace 연결 생성
        createContentPlace(content, place, i);
      }
    } else {
      log.warn("No places found in callback for contentId={}", content.getId());
    }
  }

  /**
   * 실패 Callback 처리
   */
  private void processFailedCallback(Content content, AiCallbackRequest request) {
    log.error("Processing FAILED callback for contentId={}", content.getId());

    // Content 상태를 FAILED로 변경
    content.updateStatus(ContentStatus.FAILED);
    contentRepository.save(content);
  }

  /**
   * Place를 조회하거나 생성합니다 (중복 방지)
   */
  private Place findOrCreatePlace(AiCallbackRequest.PlaceInfo placeInfo) {
    // 이름과 좌표로 기존 Place 조회
    return placeRepository.findByNameAndLatitudeAndLongitude(
            placeInfo.getName(),
            placeInfo.getLatitude(),
            placeInfo.getLongitude())
        .orElseGet(() -> {
          // 없으면 새로 생성
          Place newPlace = Place.builder()
              .name(placeInfo.getName())
              .address(placeInfo.getAddress())
              .country(placeInfo.getCountry())
              .latitude(placeInfo.getLatitude())
              .longitude(placeInfo.getLongitude())
              .description(placeInfo.getDescription())
              .build();

          Place savedPlace = placeRepository.save(newPlace);
          log.debug("Created new place: id={}, name={}", savedPlace.getId(), savedPlace.getName());
          return savedPlace;
        });
  }

  /**
   * ContentPlace 연결을 생성합니다.
   */
  private void createContentPlace(Content content, Place place, int position) {
    ContentPlace contentPlace = ContentPlace.builder()
        .content(content)
        .place(place)
        .position(position)
        .build();

    contentPlaceRepository.save(contentPlace);
    log.debug("Created ContentPlace: contentId={}, placeId={}, position={}", content.getId(), place.getId(), position);
  }
}
