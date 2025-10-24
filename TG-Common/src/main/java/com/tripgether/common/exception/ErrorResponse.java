package com.tripgether.common.exception;

import com.tripgether.common.exception.constant.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 에러 응답을 표현하는 클래스 에러 코드와 메시지를 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  private String message; // 에러 메시지

  /**
   * ErrorCode enum으로부터 에러 응답 객체를 생성하는 정적 팩토리 메소드
   *
   * @param errorCode 에러 코드 enum
   * @return 에러 응답 객체
   */
  public static ErrorResponse getResponse(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .message(errorCode.getMessage())
        .build();
  }

  /**
   * ErrorCode enum과 추가 메시지를 결합하여 에러 응답 객체를 생성하는 정적 팩토리 메소드
   *
   * @param errorCode         에러 코드 enum
   * @param additionalMessage 추가 에러 메시지
   * @return 추가 메시지가 포함된 에러 응답 객체
   */
  public static ErrorResponse getResponse(ErrorCode errorCode, String additionalMessage) {
    return ErrorResponse.builder()
        .message(errorCode.getMessage() + " - " + additionalMessage)
        .build();
  }

  /**
   * ErrorCodeBuilder로부터 에러 응답 객체를 생성하는 정적 팩토리 메소드
   */
  public static ErrorResponse getResponse(ErrorCodeBuilder errorCodeBuilder) {
    return ErrorResponse.builder()
        .message(errorCodeBuilder.getMessage())
        .build();
  }
}
