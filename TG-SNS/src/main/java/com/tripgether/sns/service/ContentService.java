package com.tripgether.sns.service;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.common.util.CommonUtil;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.place.dto.PlaceResponse;
import com.tripgether.place.entity.Place;
import com.tripgether.sns.dto.RecentContentResponse;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentPlace;
import com.tripgether.sns.repository.ContentPlaceRepository;
import com.tripgether.sns.repository.ContentRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
  private static final int MAX_URL_LENGTH = 2048;
  private static final int MAX_PHOTO_URLS_PER_PLACE = 10;

  private final ContentRepository contentRepository;
  private final ContentPlaceRepository contentPlaceRepository;
  private final MemberRepository memberRepository;
  private final AiServerService aiServerService;
  private final CommonUtil commonUtil;

  /**
   * 클라이언트로부터 장소 추출 요청 처리
   * - 같은 URL로 COMPLETED된 Content 있으면 즉시 반환 (AI 비용 절감)
   * - 없거나 PENDING/FAILED 상태면 AI 서버로 요청
   *
   * @param request 장소 추출 요청
   * @return 장소 추출 요청 결과
   */
  public RequestPlaceExtractionResponse createContentAndRequestPlaceExtraction(PlaceExtractionRequest request, UUID memberId) {
    // 회원 존재 여부 검증
    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    String snsUrl = request.getSnsUrl();

    // URL 길이 검증
    commonUtil.validateUrlLength(snsUrl, MAX_URL_LENGTH);

    // 기존 COMPLETED Content 조회 - 있으면 즉시 반환 (AI 요청 스킵)
    return contentRepository.findByOriginalUrl(snsUrl)
        .filter(content -> content.getStatus() == ContentStatus.COMPLETED)
        .map(content -> {
          // 이미 처리 완료된 데이터 반환
          log.info("Content already exists and completed. Returning existing data: contentId={}", content.getId());
          return RequestPlaceExtractionResponse.builder()
              .contentId(content.getId())
              .memberId(member.getId())
              .status(content.getStatus())
              .build();
        })
        .orElseGet(() -> processNewOrPendingContent(snsUrl, member));  // 없으면 신규/재처리
  }

  /**
   * 신규 또는 미완료 Content 처리 후 AI 서버 요청
   *
   * - 기존 Content 있으면 PENDING 상태로 변경 후 재사용
   * - 없으면 신규 Content 생성
   * - AI 서버로 장소 추출 요청 전송
   *
   * @param snsUrl SNS URL
   * @return 장소 추출 요청 결과
   */
  private RequestPlaceExtractionResponse processNewOrPendingContent(String snsUrl, Member member) {
    // Content 생성 또는 재사용
    Content content = contentRepository.findByOriginalUrl(snsUrl)
        .map(existingContent -> {
          // 기존 Content를 PENDING 상태로 변경하여 재사용
          existingContent.setStatus(ContentStatus.PENDING);
          log.info("Reusing existing Content: contentId={}", existingContent.getId());
          return existingContent;
        })
        .orElseGet(() -> {
          // 신규 Content 생성
          return Content.builder()
              .member(member)
              .originalUrl(snsUrl)
              .status(ContentStatus.PENDING)
              .build();
        });

    // Content 저장
    Content savedContent = contentRepository.save(content);
    UUID contentId = savedContent.getId();

    // AI 서버로 장소 추출 요청
    PlaceExtractionResponse placeExtractionResponse
        = aiServerService.sendPlaceExtractionRequest(contentId, snsUrl);

    // AI 서버 응답 검증
    if (placeExtractionResponse == null || !"ACCEPTED".equals(placeExtractionResponse.getStatus())) {
      throw new CustomException(ErrorCode.AI_SERVER_ERROR);
    }

    return RequestPlaceExtractionResponse.builder()
        .contentId(contentId)
        .memberId(member.getId())
        .status(savedContent.getStatus())
        .build();
  }

  /**
   * 메인 화면 - 최근 SNS 콘텐츠 목록 조회
   */
  @Transactional(readOnly = true)
  public List<RecentContentResponse> getRecentContents(UUID memberId) {

    // 회원 존재 여부 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    log.info("[Content] 최근 SNS 콘텐츠 조회 - memberId={}", memberId);

    // 최근 10개의 SNS 콘텐츠 조회
    List<Content> contents =
        contentRepository.findTop10ByMember_IdOrderByCreatedAtDesc(member.getId());

    // 응답 DTO 변환
    return contents.stream()
        .map(RecentContentResponse::fromEntity)
        .toList();
  }

  /**
   * 사용자별 저장한 장소 목록 조회 (최신순 최대 10개)
   */
  @Transactional(readOnly = true)
  public List<PlaceResponse> getSavedPlaces(UUID memberId) {
    // 회원 존재 여부 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    log.info("[Place] 저장 장소 목록 조회 - memberId={}", member.getId());

    // 이 회원이 가진 Content 들과 연결된 ContentPlace를 최신순 10개 조회
    List<ContentPlace> contentPlaces =
        contentPlaceRepository.findTop10ByContent_MemberOrderByCreatedAtDesc(member);

    // ContentPlace → Place 추출
    List<Place> places = contentPlaces.stream()
        .map(ContentPlace::getPlace)
        .toList();

    // Entity → DTO 변환 (기존 로직 재사용)
    return places.stream()
        .map(place -> PlaceResponse.builder()
            .placeId(place.getId())
            .name(place.getName())
            .address(place.getAddress())
            .rating(place.getRating())
            .photoUrls(
                place.getPhotoUrls() == null
                    ? java.util.Collections.emptyList()
                    : place.getPhotoUrls().size() > MAX_PHOTO_URLS_PER_PLACE
                        ? place.getPhotoUrls()
                        .subList(0, MAX_PHOTO_URLS_PER_PLACE)
                        : place.getPhotoUrls()
            )
            .description(place.getDescription())
            .build()
        )
        .collect(Collectors.toList());
  }
}
