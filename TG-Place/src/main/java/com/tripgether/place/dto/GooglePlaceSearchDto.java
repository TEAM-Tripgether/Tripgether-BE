package com.tripgether.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Google Places API 응답 DTO
 */
@Getter
@NoArgsConstructor
public class GooglePlaceSearchDto {

  private List<Candidate> candidates;
  private String status;

  /**
   * Google Places API 검색 결과
   */
  @Getter
  @NoArgsConstructor
  public static class Candidate {

    @JsonProperty("place_id")
    private String placeId;

    private String name;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    private Geometry geometry;
  }

  /**
   * 좌표 정보
   */
  @Getter
  @NoArgsConstructor
  public static class Geometry {
    private Location location;
  }

  /**
   * 위도/경도
   */
  @Getter
  @NoArgsConstructor
  public static class Location {
    private BigDecimal lat;
    private BigDecimal lng;
  }

  /**
   * Service 레이어에서 사용할 장소 상세 정보
   */
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PlaceDetail {
    private String placeId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String country;
  }
}
