package com.tripgether.ai.service;

import com.tripgether.ai.dto.PlaceExtractionRequest;
import com.tripgether.ai.dto.RequestPlaceExtractionResponse;
import com.tripgether.ai.dto.PlaceExtractionResponse;
import com.tripgether.common.properties.AiServerProperties;
import com.tripgether.common.util.NetworkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI 서버와의 통신을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServerService {

  private final NetworkUtil networkUtil;
  private final AiServerProperties aiServerProperties;

  /**
   * AI 서버에 장소 추출 요청을 전송합니다.
   * 비동기 요청이며, AI 서버는 202 Accepted를 즉시 반환합니다.
   * 실제 결과는 Webhook Callback으로 수신됩니다.
   *
   * @param contentId Content UUID
   * @param snsUrl    분석할 SNS URL
   * @return AI 서버 응답 (202 Accepted)
   */
  public PlaceExtractionResponse requestPlaceExtractionToAiServer(UUID contentId, String snsUrl) {
    String aiServerPlaceExtractionUrl = aiServerProperties.getBaseUrl() + aiServerProperties.getExtractPlacesUri();

    PlaceExtractionRequest aiContentRequest = PlaceExtractionRequest.builder()
            .contentId(contentId)
            .snsUrl(snsUrl)
            .build();

    Map<String, String> headers = new HashMap<>();
    headers.put("X-API-Key", aiServerProperties.getApiKey());

    log.info("Requesting place extraction to AI server: contentId={}, snsUrl={}", contentId, snsUrl);

    PlaceExtractionResponse response = networkUtil.sendPostRequest(
        aiServerPlaceExtractionUrl,
        aiContentRequest,
        headers,
        PlaceExtractionResponse.class
    );

    log.info("AI server accepted the request: contentId={}, status={}", contentId, response.getStatus());

    return response;
  }
}
