package com.tripgether.place.service;

import com.tripgether.place.constant.PlacePlatform;
import com.tripgether.place.dto.GooglePlaceSearchDto;

/**
 * 플랫폼별 장소 검색 인터페이스
 * <p>
 * Google, Kakao, Naver 등 여러 플랫폼의 장소 검색 API를 통합하기 위한 인터페이스
 */
public interface PlacePlatformSearcher {

  /**
   * 지원하는 플랫폼 반환
   *
   * @return 플랫폼 (GOOGLE, KAKAO, NAVER)
   */
  PlacePlatform getSupportedPlatform();

  /**
   * 상호명으로 장소 상세 정보 검색
   *
   * @param placeName 장소명
   * @param address   주소 (fallback용, 현재 미사용)
   * @param language  언어 코드 (ko, en, ja, zh)
   * @return 장소 상세 정보 (place_id, 좌표, 추가 정보 등)
   */
  GooglePlaceSearchDto.PlaceDetail searchPlaceDetail(String placeName, String address, String language);
}
