package com.tripgether.sns.service;

import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.place.entity.Place;
import com.tripgether.sns.dto.ContentDto;
import com.tripgether.sns.dto.GetContentInfoResponse;
import com.tripgether.sns.dto.RequestPlaceExtractionRequest;
import com.tripgether.sns.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.common.util.CommonUtil;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.place.dto.PlaceDto;
import com.tripgether.place.entity.Place;
import com.tripgether.sns.dto.ContentDto;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentPlace;
import com.tripgether.sns.repository.ContentPlaceRepository;
import com.tripgether.sns.repository.ContentRepository;
import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

  private static final int MAX_URL_LENGTH = 2048;
  private static final int MAX_PHOTO_URLS_PER_PLACE = 10;

  private final ContentRepository contentRepository;
  private final ContentPlaceRepository contentPlaceRepository;
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
   * @param memberId 회원 ID
   * @return 장소 추출 요청 결과
   */
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public RequestPlaceExtractionResponse createContentAndRequestPlaceExtraction(
      RequestPlaceExtractionRequest request,
      UUID memberId) {
    String snsUrl = request.getSnsUrl();

    // URL 길이 검증
    commonUtil.validateUrlLength(snsUrl, MAX_URL_LENGTH);

    // 기존 Content 조회 (한 번만 수행)
    Optional<Content> optionalContent = contentRepository.findByOriginalUrl(snsUrl);

    // 이미 COMPLETED면 즉시 반환
    if (optionalContent.isPresent() && optionalContent.get().getStatus() == ContentStatus.COMPLETED) {
      Content content = optionalContent.get();
      log.info("Content already exists and completed. Returning existing data: contentId={}", content.getId());
      return RequestPlaceExtractionResponse.builder()
          .contentId(content.getId())
          .status(content.getStatus())
          .build();
    }

    // 기존이 있으면 PENDING으로 재사용, 없으면 신규 생성
    Content content = optionalContent
        .map(existingContent -> {
          existingContent.setStatus(ContentStatus.PENDING);
          existingContent.setMemberId(memberId);
          log.info("Reusing existing Content: contentId={}", existingContent.getId());
          return existingContent;
        })
        .orElseGet(() -> Content.builder()
            .originalUrl(snsUrl)
            .status(ContentStatus.PENDING)
            .memberId(memberId)
            .build());

    // Content 저장
    Content savedContent = contentRepository.save(content);

    // AI 요청
    try {
      requestAIContentAnalyze(savedContent);
    } catch (CustomException e) {
      // 요청 실패시 FAIL 처리
      savedContent.setStatus(ContentStatus.FAILED);
      contentRepository.save(savedContent);
      throw e;
    }

    return RequestPlaceExtractionResponse.builder()
        .contentId(savedContent.getId())
        .status(savedContent.getStatus())
        .build();
  }

  /**
   * ContentId와 함께 AI 서버 요청
   */
  private void requestAIContentAnalyze(Content content) {
    UUID contentId = content.getId();
    String snsUrl = content.getOriginalUrl();

    // AI 서버로 장소 추출 요청
    PlaceExtractionResponse response
        = aiServerService.sendPlaceExtractionRequest(contentId, snsUrl);

    // AI 서버 응답 검증
    // AI 서버는 {"received": true, "contentId": "..."} 형식으로 응답
    if (response == null || !Boolean.TRUE.equals(response.getReceived())) {
      log.error("AI server did not accept the request: contentId={}, received={}, status={}",
          contentId, response != null ? response.getReceived() : null,
          response != null ? response.getStatus() : null);
      throw new CustomException(ErrorCode.AI_SERVER_ERROR);
    }

    log.info("AI server successfully accepted place extraction request: contentId={}", contentId);
  }

  /**
   * Content 정보 및 연관된 Place 목록 조회
   * - Content가 존재하지 않으면 예외 발생
   * - ContentPlace를 통해 연관된 Place 목록을 position 순서대로 조회
   *
   * @param contentId 조회할 Content ID
   * @return Content 정보 및 연관된 Place 목록
   */
  @Transactional(readOnly = true)
  public GetContentInfoResponse getContentInfo(UUID contentId) {
    // Content 조회
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> {
          log.error("Content not found: contentId={}", contentId);
          return new CustomException(ErrorCode.CONTENT_NOT_FOUND);
        });

    log.info("Content found: contentId={}, status={}", contentId, content.getStatus());

    // ContentPlace 목록 조회 (Fetch Join으로 Place도 함께 조회, N+1 문제 해결)
    List<ContentPlace> contentPlaces = contentPlaceRepository.findByContentIdWithPlace(contentId);

    // Place 목록 추출
    List<Place> places = contentPlaces.stream()
        .map(ContentPlace::getPlace)
        .collect(Collectors.toList());

    log.info("Found {} places for contentId={}", places.size(), contentId);

    // DTO 변환 후 반환
    return GetContentInfoResponse.from(content, places);
  }

  /**
   * Member가 소유한 Content 목록 조회 (최신순)
   * - Member ID로 Content 페이지를 조회합니다.
   * - createdAt 기준 내림차순 정렬 (최신순)
   * - Place 정보는 제외하고 Content 정보만 반환
   *
   * @param memberId 회원 ID
   * @param pageSize 페이지 크기
   * @return Content 페이지 (ContentDto)
   */
  @Transactional(readOnly = true)
  public Page<ContentDto> getMemberContentPage(UUID memberId, int pageSize) {
    // Pageable 생성 (0번 페이지, createdAt 내림차순)
    Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    // Member ID로 Content 페이지 조회
    Page<Content> contentPage = contentRepository.findByMemberId(memberId, pageable);

    log.info("Found {} contents for memberId={}, pageSize={}",
        contentPage.getTotalElements(), memberId, pageSize);

    // Entity -> DTO 변환
    return contentPage.map(ContentDto::from);
  }

  /**
   * 메인 화면 - 최근 SNS 콘텐츠 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ContentDto> getRecentContents(UUID memberId) {

    // 회원 존재 여부 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    log.info("[Content] 최근 SNS 콘텐츠 조회 - memberId={}", memberId);

    // 최근 10개의 SNS 콘텐츠 조회
    List<Content> contents =
        contentRepository.findTop10ByMember_IdOrderByCreatedAtDesc(member.getId());

    // 응답 DTO 변환
    return contents.stream()
        .map(ContentDto::fromEntity)
        .toList();
  }

  /**
   * 사용자별 저장한 장소 목록 조회 (최신순 최대 10개)
   */
  @Transactional(readOnly = true)
  public List<PlaceDto> getSavedPlaces(UUID memberId) {
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

    // Entity → DTO 변환
    return places.stream()
        .map(place -> PlaceDto.builder()
            .placeId(place.getId())
            .name(place.getName())
            .address(place.getAddress())
            .rating(place.getRating())
            .photoUrls(
                Optional.ofNullable(place.getPhotoUrls())
                    .map(urls -> urls.size() > MAX_PHOTO_URLS_PER_PLACE
                        ? urls.subList(0, MAX_PHOTO_URLS_PER_PLACE)
                        : urls)
                    .orElse(Collections.emptyList())
            )
            .description(place.getDescription())
            .build()
        )
        .toList();
  }
}
