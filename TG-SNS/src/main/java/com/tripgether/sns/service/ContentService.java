package com.tripgether.sns.service;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.service.AiServerService;
import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.repository.ContentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
  private final ContentRepository contentRepository;
  private final AiServerService aiServerService;
  /**
   * 클라이언트로부터 장소 추출 요청을 처리합니다.
   *
   * @param request 장소 추출 요청 데이터
   * @return 장소 추출 요청 처리 결과
   */
  public RequestPlaceExtractionResponse createContentAndRequestPlaceExtraction(PlaceExtractionRequest request) {
    // SNS url 추출
    String snsUrl = request.getSnsUrl();

    // Content 객체 생성 createContent
    Content content = Content.builder()
        .originalUrl(snsUrl)
        .status(ContentStatus.PENDING)
        .build();

    // Content 저장
    Content savedContent = contentRepository.save(content);
    UUID contentId = savedContent.getId();

    // AI 서버 Content 정보 요청 RequestPlaceExtraction
    PlaceExtractionResponse placeExtractionResponse
        = aiServerService.sendPlaceExtractionRequest(contentId, snsUrl);

    // AI 서버 비정상 응답
    if(placeExtractionResponse == null || !"ACCEPTED".equals(placeExtractionResponse.getStatus())){
      throw new CustomException(ErrorCode.AI_SERVER_ERROR);
    }

    return RequestPlaceExtractionResponse.builder()
        .contentId(contentId)
        .status(savedContent.getStatus())
        .build();
  }
}
