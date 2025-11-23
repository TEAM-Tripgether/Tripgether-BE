package com.tripgether.place.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.place.dto.PlaceDetailDto;
import com.tripgether.place.entity.Place;
import com.tripgether.place.entity.PlaceBusinessHour;
import com.tripgether.place.entity.PlaceMedia;
import com.tripgether.place.entity.PlacePlatformReference;
import com.tripgether.place.repository.PlaceBusinessHourRepository;
import com.tripgether.place.repository.PlaceMediaRepository;
import com.tripgether.place.repository.PlacePlatformReferenceRepository;
import com.tripgether.place.repository.PlaceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

  private final PlaceRepository placeRepository;
  private final PlacePlatformReferenceRepository placePlatformReferenceRepository;
  private final PlaceBusinessHourRepository placeBusinessHourRepository;
  private final PlaceMediaRepository placeMediaRepository;

  /**
   * 장소 상세 정보 조회
   * - Place 기본 정보
   * - 플랫폼별 참조 정보 (Google Place ID 등)
   * - 영업시간
   * - 추가 미디어
   *
   * @param placeId 조회할 장소 ID
   * @return 장소 상세 정보 DTO
   */
  @Transactional(readOnly = true)
  public PlaceDetailDto getPlaceDetail(UUID placeId) {
    // 1. Place 조회
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> {
          log.error("Place not found: placeId={}", placeId);
          return new CustomException(ErrorCode.PLACE_NOT_FOUND);
        });

    log.info("Place found: placeId={}, name={}", placeId, place.getName());

    // 2. PlacePlatformReference 조회 (Google Place ID 등)
    List<PlacePlatformReference> platformReferences =
        placePlatformReferenceRepository.findByPlace(place);

    // 3. PlaceBusinessHour 조회 (요일 순서)
    List<PlaceBusinessHour> businessHours =
        placeBusinessHourRepository.findByPlaceIdOrderByWeekday(placeId);

    // 4. PlaceMedia 조회 (position 순서)
    List<PlaceMedia> medias =
        placeMediaRepository.findByPlaceIdOrderByPosition(placeId);

    log.info("Found {} platform references, {} business hours, {} medias for placeId={}",
        platformReferences.size(), businessHours.size(), medias.size(), placeId);

    // 5. DTO 변환 후 반환
    return PlaceDetailDto.from(place, platformReferences, businessHours, medias);
  }
}
