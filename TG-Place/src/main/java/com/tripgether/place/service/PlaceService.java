package com.tripgether.place.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.place.dto.PlaceResponse;
import com.tripgether.place.entity.Place;
import com.tripgether.place.repository.PlaceRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceService {
  int MAX_PHOTO_URLS_PER_PLACE = 10;

  private final PlaceRepository placeRepository;
  private final MemberRepository memberRepository;

  /**
   * 사용자별 저장한 장소 목록 조회 (최신순 최대 10개)
   */
  @Transactional(readOnly = true)
  public List<PlaceResponse> getSavedPlaces(UUID memberId) {
    // 회원 존재 여부 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    log.info("[Place] 저장 장소 목록 조회 - memberId={}", member.getId());

    List<Place> places =
        placeRepository.findTop10ByMember_IdOrderByCreatedAtDesc(memberId);

    // Entity -> DTO 변환
    return places.stream()
        .map(place -> PlaceResponse.builder()
            .placeId(place.getId())
            .name(place.getName())
            .address(place.getAddress())
            .rating(place.getRating())
            .photoUrls(
                place.getPhotoUrls() != null && place.getPhotoUrls().size() > 10
                    ? place.getPhotoUrls().subList(0, MAX_PHOTO_URLS_PER_PLACE)
                    : place.getPhotoUrls()
            )
            .description(place.getDescription())
            .build()
        )
        .collect(Collectors.toList());
  }
}
