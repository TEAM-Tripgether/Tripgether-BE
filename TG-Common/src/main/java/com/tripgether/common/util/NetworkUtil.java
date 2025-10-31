package com.tripgether.common.util;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP 통신을 위한 유틸리티 클래스
 * WebClient를 활용하여 외부 API 호출을 수행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkUtil {

  private final WebClient webClient;

  /**
   * POST 요청을 전송합니다.
   *
   * @param url          요청 URL
   * @param requestBody  요청 본문
   * @param headers      HTTP 헤더 (nullable)
   * @param responseType 응답 타입 클래스
   * @param <T>          응답 타입
   * @return 응답 객체
   * @throws CustomException 네트워크 오류 또는 API 오류 발생 시
   */
  public <T> T sendPostRequest(String url, Object requestBody,
                                Map<String, String> headers,
                                Class<T> responseType) {
    try {
      log.debug("Sending POST request to: {}", url);
      log.debug("Request body: {}", requestBody);

      return webClient.post()
          .uri(url)
          .headers(httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
              headers.forEach(httpHeaders::set);
            }
          })
          .bodyValue(requestBody)
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, response -> {
            log.error("Client error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR));
          })
          .onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("Server error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.AI_SERVER_ERROR));
          })
          .bodyToMono(responseType)
          .block();

    } catch (WebClientException e) {
      log.error("WebClient error: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.NETWORK_ERROR);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during HTTP request: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * GET 요청을 전송합니다.
   *
   * @param url          요청 URL
   * @param headers      HTTP 헤더 (nullable)
   * @param responseType 응답 타입 클래스
   * @param <T>          응답 타입
   * @return 응답 객체
   * @throws CustomException 네트워크 오류 또는 API 오류 발생 시
   */
  public <T> T sendGetRequest(String url, Map<String, String> headers,
                               Class<T> responseType) {
    try {
      log.debug("Sending GET request to: {}", url);

      return webClient.get()
          .uri(url)
          .headers(httpHeaders -> {
            if (headers != null) {
              headers.forEach(httpHeaders::set);
            }
          })
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, response -> {
            log.error("Client error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR));
          })
          .onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("Server error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.AI_SERVER_ERROR));
          })
          .bodyToMono(responseType)
          .block();

    } catch (WebClientException e) {
      log.error("WebClient error: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.NETWORK_ERROR);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during HTTP request: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
