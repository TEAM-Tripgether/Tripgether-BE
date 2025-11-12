package com.tripgether.place.service;

import com.tripgether.common.properties.PlaceProperties;
import com.tripgether.common.util.NetworkUtil;
import com.tripgether.place.dto.GooglePlaceSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Google Places API를 통한 장소 검색 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceSearcher {

  private final NetworkUtil networkUtil;
  private final PlaceProperties placeProperties;

  private static final String GOOGLE_PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
  private static final String SEARCH_FIELDS = "place_id,name,formatted_address,geometry";

  /**
   * 상호명으로 Google Place 검색
   *
   * @param placeName 장소명
   * @param address   주소 (fallback용, 현재 미사용)
   * @param language  언어 코드 (ko, en, ja, zh)
   * @return Google Place 상세 정보 (place_id, 좌표 등)
   */
  public GooglePlaceSearchDto.PlaceDetail searchPlaceDetail(String placeName, String address, String language) {
    String googleApiKey = placeProperties.getGoogleApiKey();

    if (googleApiKey == null || googleApiKey.isEmpty()) {
      log.warn("Google Places API key not configured");
      return null;
    }

    try {
      // URL 생성
      String url = buildSearchUrl(placeName, language, googleApiKey);

      log.debug("Google Places API request: placeName={}, language={}", placeName, language);

      // API 호출
      GooglePlaceSearchDto response = networkUtil.sendGetRequest(
          url,
          null,  // headers 없음 (URL에 key 포함)
          GooglePlaceSearchDto.class
      );

      // 결과 파싱
      if ("OK".equals(response.getStatus())
          && response.getCandidates() != null
          && !response.getCandidates().isEmpty()) {

        GooglePlaceSearchDto.Candidate candidate = response.getCandidates().get(0);

        GooglePlaceSearchDto.PlaceDetail placeDetail = GooglePlaceSearchDto.PlaceDetail.builder()
            .placeId(candidate.getPlaceId())
            .name(candidate.getName())
            .address(candidate.getFormattedAddress())
            .latitude(candidate.getGeometry().getLocation().getLat())
            .longitude(candidate.getGeometry().getLocation().getLng())
            .country(extractCountryCode(candidate.getFormattedAddress()))
            .build();

        log.info("Google Place found: placeId={}, name={}", placeDetail.getPlaceId(), placeDetail.getName());
        return placeDetail;

      } else {
        log.warn("No place found from Google: placeName={}, status={}", placeName, response.getStatus());
        return null;
      }

    } catch (Exception e) {
      log.error("Google Places API error: placeName={}", placeName, e);
      return null;
    }
  }

  /**
   * Google Places API 검색 URL 생성
   *
   * @param placeName    장소명
   * @param language     언어 코드
   * @param googleApiKey API 키
   * @return 검색 URL
   */
  private String buildSearchUrl(String placeName, String language, String googleApiKey) {
    String encodedPlaceName = URLEncoder.encode(placeName, StandardCharsets.UTF_8);

    return String.format("%s?input=%s&inputtype=textquery&fields=%s&language=%s&key=%s",
        GOOGLE_PLACES_BASE_URL,
        encodedPlaceName,
        SEARCH_FIELDS,
        language,
        googleApiKey
    );
  }

  /**
   * 주소에서 국가 코드 추출
   * <p>
   * 간단 구현: 주소 문자열에서 국가명 패턴 매칭
   * 추후 더 정교한 로직으로 개선 필요
   *
   * @param address Google API의 formatted_address
   * @return 국가 코드 (ISO 3166-1 alpha-2)
   */
  private String extractCountryCode(String address) {
    if (address == null || address.isEmpty()) {
      return "XX";
    }

    // 한국
    if (address.contains("South Korea") || address.contains("대한민국") || address.contains("Korea")) {
      return "KR";
    }
    // 미국
    if (address.contains("United States") || address.contains("USA") || address.contains("US")) {
      return "US";
    }
    // 일본
    if (address.contains("Japan") || address.contains("日本")) {
      return "JP";
    }
    // 중국
    if (address.contains("China") || address.contains("中国")) {
      return "CN";
    }

    // 기본값
    log.debug("Could not extract country code from address: {}", address);
    return "XX";
  }
}
