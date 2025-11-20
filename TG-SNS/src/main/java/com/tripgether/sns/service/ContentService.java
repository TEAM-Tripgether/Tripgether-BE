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
import com.tripgether.sns.dto.RecentContentResponse;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.repository.ContentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
  private static final int MAX_URL_LENGTH = 2048;

  private final ContentRepository contentRepository;
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
              .memberId(memberId)
              .status(content.getStatus())
              .build();
        })
        .orElseGet(() -> processNewOrPendingContent(snsUrl, memberId));  // 없으면 신규/재처리
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
  private RequestPlaceExtractionResponse processNewOrPendingContent(String snsUrl, UUID memberId) {
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
              .member(memberRepository.findById(memberId)
                  .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)))
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
        .memberId(memberId)
        .status(savedContent.getStatus())
        .build();
  }
}
