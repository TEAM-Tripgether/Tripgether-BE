package com.tripgether.sns.service;

import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.sns.dto.RequestPlaceExtractionRequest;
import com.tripgether.sns.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.common.util.CommonUtil;
import com.tripgether.member.entity.Member;
import com.tripgether.member.repository.MemberRepository;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.entity.ContentMember;
import com.tripgether.sns.repository.ContentMemberRepository;
import com.tripgether.sns.repository.ContentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

  private static final int MAX_URL_LENGTH = 2048;

  private final ContentRepository contentRepository;
  private final ContentMemberRepository contentMemberRepository;
  private final MemberRepository memberRepository;
  private final AiServerService aiServerService;
  private final CommonUtil commonUtil;

  /**
   * 클라이언트로부터 장소 추출 요청 처리
   * - 같은 URL로 COMPLETED된 Content 있으면 즉시 반환하고 ContentMember 추가 (AI 비용 절감)
   * - 없거나 PENDING/FAILED 상태면 AI 서버로 요청하고 ContentMember 생성
   *
   * @param memberId 요청한 회원 ID
   * @param request 장소 추출 요청
   * @return 장소 추출 요청 결과
   */
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public RequestPlaceExtractionResponse createContentAndRequestPlaceExtraction(UUID memberId, RequestPlaceExtractionRequest request) {
    String snsUrl = request.getSnsUrl();

    // URL 길이 검증
    commonUtil.validateUrlLength(snsUrl, MAX_URL_LENGTH);

    // Member 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 기존 Content 조회 (한 번만 수행)
    Optional<Content> optionalContent = contentRepository.findByOriginalUrl(snsUrl);

    // 이미 COMPLETED면 ContentMember 추가 후 즉시 반환
    if (optionalContent.isPresent() && optionalContent.get().getStatus() == ContentStatus.COMPLETED) {
      Content content = optionalContent.get();
      log.info("Content already exists and completed. Adding ContentMember: contentId={}, memberId={}", content.getId(), memberId);

      // ContentMember가 없으면 생성 (중복 방지)
      if (!contentMemberRepository.existsByContentAndMember(content, member)) {
        ContentMember contentMember = ContentMember.builder()
            .content(content)
            .member(member)
            .notified(true)  // 이미 완료된 Content이므로 알림 불필요
            .build();
        contentMemberRepository.save(contentMember);
        log.info("Created ContentMember for existing completed Content: contentId={}, memberId={}", content.getId(), memberId);
      }

      return RequestPlaceExtractionResponse.builder()
          .contentId(content.getId())
          .status(content.getStatus())
          .build();
    }

    // 기존이 있으면 PENDING으로 재사용, 없으면 신규 생성
    Content content = optionalContent
        .map(existingContent -> {
          existingContent.setStatus(ContentStatus.PENDING);
          log.info("Reusing existing Content: contentId={}", existingContent.getId());
          return existingContent;
        })
        .orElseGet(() -> Content.builder()
            .originalUrl(snsUrl)
            .status(ContentStatus.PENDING)
            .build());

    // Content 저장
    Content savedContent = contentRepository.save(content);

    // ContentMember 생성 (중복 방지)
    if (!contentMemberRepository.existsByContentAndMember(savedContent, member)) {
      ContentMember contentMember = ContentMember.builder()
          .content(savedContent)
          .member(member)
          .notified(false)  // 분석 완료시 알림 전송 필요
          .build();
      contentMemberRepository.save(contentMember);
      log.info("Created ContentMember: contentId={}, memberId={}", savedContent.getId(), memberId);
    } else {
      log.info("ContentMember already exists: contentId={}, memberId={}", savedContent.getId(), memberId);
    }

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
}
