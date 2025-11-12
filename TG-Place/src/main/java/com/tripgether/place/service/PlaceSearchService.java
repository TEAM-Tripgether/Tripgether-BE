package com.tripgether.place.service;

import com.tripgether.place.constant.PlacePlatform;
import com.tripgether.place.dto.GooglePlaceSearchDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 플랫폼별 장소 검색 Facade 서비스
 * <p>
 * Google, Kakao, Naver 등 여러 플랫폼의 검색기를 통합 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchService {

  private final List<PlacePlatformSearcher> searchers;
  private Map<PlacePlatform, PlacePlatformSearcher> searcherMap;

  /**
   * 초기화: searchers를 Map으로 변환하여 플랫폼별 빠른 조회
   */
  @PostConstruct
  public void init() {
    searcherMap = searchers.stream()
        .collect(Collectors.toMap(
            PlacePlatformSearcher::getSupportedPlatform,
            Function.identity()
        ));

    log.info("Initialized PlaceSearchService with {} platform searchers: {}",
        searcherMap.size(), searcherMap.keySet());
  }

  /**
   * 특정 플랫폼으로 장소 검색
   *
   * @param platform  플랫폼 (GOOGLE, KAKAO, NAVER)
   * @param placeName 장소명
   * @param address   주소
   * @param language  언어 코드
   * @return 장소 상세 정보
   */
  public GooglePlaceSearchDto.PlaceDetail searchPlace(
      PlacePlatform platform,
      String placeName,
      String address,
      String language) {

    PlacePlatformSearcher searcher = searcherMap.get(platform);

    if (searcher == null) {
      log.warn("Unsupported platform: {}", platform);
      return null;
    }

    return searcher.searchPlaceDetail(placeName, address, language);
  }

  /**
   * Google 플랫폼으로 장소 검색 (단축 메서드)
   *
   * @param placeName 장소명
   * @param address   주소
   * @param language  언어 코드
   * @return 장소 상세 정보
   */
  public GooglePlaceSearchDto.PlaceDetail searchGooglePlace(
      String placeName,
      String address,
      String language) {

    return searchPlace(PlacePlatform.GOOGLE, placeName, address, language);
  }
}
