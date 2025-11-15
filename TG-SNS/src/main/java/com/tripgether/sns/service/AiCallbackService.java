package com.tripgether.sns.service;

import com.tripgether.ai.dto.AiCallbackRequest;
import com.tripgether.ai.dto.PlaceInfo;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.place.constant.PlacePlatform;
import com.tripgether.place.dto.GooglePlaceSearchDto;
import com.tripgether.place.entity.Place;
import com.tripgether.place.entity.PlacePlatformReference;
import com.tripgether.place.repository.PlacePlatformReferenceRepository;
import com.tripgether.place.repository.PlaceRepository;
import com.tripgether.place.service.PlaceSearchService;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentPlace;
import com.tripgether.sns.repository.ContentPlaceRepository;
import com.tripgether.sns.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// AI 서버 Webhook Callback 처리
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCallbackService {

  private final ContentRepository contentRepository;
  private final PlaceRepository placeRepository;
  private final ContentPlaceRepository contentPlaceRepository;
  private final PlacePlatformReferenceRepository placePlatformReferenceRepository;
  private final PlaceSearchService placeSearchService;

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
   * - Google Places API 호출하여 place_id 및 상세 정보 획득
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
      contentPlaceRepository.deleteByContentIdWithFlush(content.getId());
      log.debug("Deleted existing ContentPlaces for contentId={}", content.getId());
    }

    // Content 상태를 COMPLETED로 변경 (신규 또는 재처리 모두)
    content.setStatus(ContentStatus.COMPLETED);
    contentRepository.save(content);

    // AI 서버에서 받은 Place 정보로 ContentPlace 생성
    if (request.getPlaces() != null && !request.getPlaces().isEmpty()) {
      List<PlaceInfo> places = request.getPlaces();
      log.info("Processing {} places for contentId={} (update mode: {})",
          places.size(), content.getId(), isContentAlreadyCompleted);

      // 각 Place 정보를 순회하며 저장
      for (int i = 0; i < places.size(); i++) {
        PlaceInfo placeInfo = places.get(i);

        log.info("Processing place {}/{}", i + 1, places.size());
        log.info("Place Name: {}", placeInfo.getName());
        log.info("Address: {}", placeInfo.getAddress());
        log.info("Language: {}", placeInfo.getLanguage());

        try {
          // 1. Google Places API 호출 (실패 시 CustomException 발생)
          GooglePlaceSearchDto.PlaceDetail googlePlace = placeSearchService.searchGooglePlace(
              placeInfo.getName(),
              placeInfo.getAddress(),
              placeInfo.getLanguage()
          );

          // 2. Google 응답으로 Place 생성/업데이트
          Place place = createOrUpdatePlace(googlePlace);

          // 3. PlacePlatformReference 저장 (Google place_id)
          savePlacePlatformReference(place, googlePlace.getPlaceId());

          // 4. Content와 Place 연결 생성 (position 포함)
          createContentPlace(content, place, i);

          log.info("Place processed successfully: DB ID={}", place.getId());

        } catch (CustomException e) {
          log.error("Failed to process place {}/{}", i + 1, places.size());
          log.error("Place Name: {}", placeInfo.getName());
          log.error("HTTP Status: {}", e.getStatus());
          log.error("Error Message: {}", e.getMessage());

          // 예외 재발생으로 전체 트랜잭션 롤백
          throw e;
        }
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
   * Google Places API 응답으로 Place 생성 또는 업데이트
   * <p>
   * 이름+좌표로 중복 체크 후 없으면 신규 생성
   *
   * @param googlePlace Google Places API 응답
   * @return 조회 또는 생성된 Place
   */
  private Place createOrUpdatePlace(GooglePlaceSearchDto.PlaceDetail googlePlace) {
    // 이름+좌표로 중복 체크
    Optional<Place> existing = placeRepository.findByNameAndLatitudeAndLongitude(
        googlePlace.getName(),
        googlePlace.getLatitude(),
        googlePlace.getLongitude()
    );

    if (existing.isPresent()) {
      // 기존 Place 업데이트
      Place place = existing.get();
      place.setAddress(googlePlace.getAddress());
      place.setCountry(googlePlace.getCountry());
      place.setTypes(googlePlace.getTypes());
      place.setBusinessStatus(googlePlace.getBusinessStatus());
      place.setIconUrl(googlePlace.getIconUrl());
      place.setRating(googlePlace.getRating());
      place.setUserRatingsTotal(googlePlace.getUserRatingsTotal());
      place.setPhotoUrls(googlePlace.getPhotoUrls());
      log.debug("Updated existing place: id={}, name={}, rating={}",
          place.getId(), place.getName(), place.getRating());
      return placeRepository.save(place);
    } else {
      // 새로 생성
      Place newPlace = Place.builder()
          .name(googlePlace.getName())
          .address(googlePlace.getAddress())
          .country(googlePlace.getCountry())
          .latitude(googlePlace.getLatitude())
          .longitude(googlePlace.getLongitude())
          .types(googlePlace.getTypes())
          .businessStatus(googlePlace.getBusinessStatus())
          .iconUrl(googlePlace.getIconUrl())
          .rating(googlePlace.getRating())
          .userRatingsTotal(googlePlace.getUserRatingsTotal())
          .photoUrls(googlePlace.getPhotoUrls())
          .build();

      Place savedPlace = placeRepository.save(newPlace);
      log.debug("Created new place: id={}, name={}, rating={}",
          savedPlace.getId(), savedPlace.getName(), savedPlace.getRating());
      return savedPlace;
    }
  }

  /**
   * PlacePlatformReference 저장
   * <p>
   * Google place_id를 PlacePlatformReference에 저장 (중복 체크)
   *
   * @param place         장소
   * @param googlePlaceId Google place_id
   */
  private void savePlacePlatformReference(Place place, String googlePlaceId) {
    log.info("Saving PlacePlatformReference: placeId={}, googlePlaceId={}", place.getId(), googlePlaceId);

    Optional<PlacePlatformReference> existing =
        placePlatformReferenceRepository.findByPlaceAndPlacePlatform(place, PlacePlatform.GOOGLE);

    if (existing.isEmpty()) {
      PlacePlatformReference ref = PlacePlatformReference.builder()
          .place(place)
          .placePlatform(PlacePlatform.GOOGLE)
          .placePlatformId(googlePlaceId)
          .build();
      placePlatformReferenceRepository.save(ref);
      log.info("Successfully saved NEW PlacePlatformReference: refId={}, placeId={}, googlePlaceId={}",
          ref.getId(), place.getId(), googlePlaceId);
    } else {
      log.info("PlacePlatformReference already exists (skipping save): refId={}, placeId={}, existingPlaceId={}, newPlaceId={}",
          existing.get().getId(), place.getId(), existing.get().getPlacePlatformId(), googlePlaceId);
    }
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
