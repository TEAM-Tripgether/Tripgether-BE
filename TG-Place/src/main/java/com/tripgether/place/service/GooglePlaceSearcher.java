package com.tripgether.place.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.properties.PlaceProperties;
import com.tripgether.common.util.NetworkUtil;
import com.tripgether.place.dto.GooglePlaceSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Google Places API를 통한 장소 검색 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceSearcher implements PlacePlatformSearcher {

  private final NetworkUtil networkUtil;
  private final PlaceProperties placeProperties;

  private static final String GOOGLE_PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
  private static final String SEARCH_FIELDS = "place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total";

  /**
   * 상호명으로 Google Place 검색
   *
   * @param placeName 장소명
   * @param address   주소 (fallback용, 현재 미사용)
   * @param language  언어 코드 (ko, en, ja, zh)
   * @return Google Place 상세 정보 (place_id, 좌표 등)
   * @throws CustomException Google Places API 호출 실패 또는 장소를 찾을 수 없을 때
   */
  @Override
  public GooglePlaceSearchDto.PlaceDetail searchPlaceDetail(String placeName, String address, String language) {
    String googleApiKey = placeProperties.getGoogleApiKey();

    if (googleApiKey == null || googleApiKey.isEmpty()) {
      log.error("Google Places API key not configured: placeName={}", placeName);
      throw new CustomException(ErrorCode.INVALID_API_KEY);
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
      String status = response.getStatus();
      
      if ("OK".equals(status)
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
            .types(candidate.getTypes())
            .businessStatus(candidate.getBusinessStatus())
            .iconUrl(candidate.getIcon())
            .rating(candidate.getRating())
            .userRatingsTotal(candidate.getUserRatingsTotal())
            .photoUrls(buildPhotoUrls(candidate.getPhotos(), googleApiKey))
            .build();

        log.info("Google Place found: placeId={}, name={}, rating={}",
            placeDetail.getPlaceId(), placeDetail.getName(), placeDetail.getRating());
        return placeDetail;

      } else {
        // Google Places API 상태 코드별 에러 처리
        log.error("Google Places API error: placeName={}, status={}", placeName, status);
        
        if ("REQUEST_DENIED".equals(status)) {
          throw new CustomException(ErrorCode.INVALID_API_KEY);
        } else if ("INVALID_REQUEST".equals(status)) {
          throw new CustomException(ErrorCode.INVALID_REQUEST);
        } else if ("OVER_QUERY_LIMIT".equals(status)) {
          throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        } else if ("ZERO_RESULTS".equals(status)) {
          throw new CustomException(ErrorCode.GOOGLE_PLACE_NOT_FOUND);
        } else {
          throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
        }
      }

    } catch (CustomException e) {
      // NetworkUtil에서 발생한 CustomException은 그대로 전파
      log.error("Google Places API error: placeName={}, error={}", placeName, e.getMessage());
      throw e;
    } catch (Exception e) {
      // 예상치 못한 예외
      log.error("Unexpected error during Google Places API call: placeName={}", placeName, e);
      throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
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
   * 사진 URL 배열 생성
   * <p>
   * photo_reference를 실제 Google Photos API URL로 변환
   *
   * @param photos       사진 정보 리스트
   * @param googleApiKey API 키
   * @return 사진 URL 배열 (최대 10개)
   */
  private List<String> buildPhotoUrls(List<GooglePlaceSearchDto.Photo> photos, String googleApiKey) {
    if (photos == null || photos.isEmpty()) {
      return null;
    }

    return photos.stream()
        .limit(10)  // 최대 10개
        .map(photo -> String.format(
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s&key=%s",
            photo.getPhotoReference(),
            googleApiKey
        ))
        .collect(Collectors.toList());
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
