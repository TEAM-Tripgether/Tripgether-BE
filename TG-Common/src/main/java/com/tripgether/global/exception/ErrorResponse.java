package com.tripgether.global.exception;

import com.tripgether.global.constant.ErrorCode;
import com.tripgether.global.constant.ErrorCodeContainer;
import com.tripgether.global.constant.MessageComponent.Action;
import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.util.MessageUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * API 에러 응답을 표현하는 클래스
 * 에러 코드와 메시지를 포함
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String code; // 에러 코드
    private String message; // 에러 메시지

    /**
     * ErrorCode enum으로부터 에러 응답 객체를 생성하는 정적 팩토리 메소드
     *
     * @param errorCode 에러 코드 enum
     * @return 에러 응답 객체
     */
    public static ErrorResponse getResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
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
                .code(errorCode.getCode())
                .message(errorCode.getMessage() + " - " + additionalMessage)
                .build();
    }

    /**
     * Subject와 Action을 이용해 동적으로 실패 메시지를 생성하는 정적 팩토리 메소드
     */
    public static ErrorResponse getFailResponse(String code, Subject subject, Action action) {
        return ErrorResponse.builder()
                .code(code)
                .message(MessageUtil.failMessage(subject, action))
                .build();
    }

    /**
     * ErrorCodeContainer로부터 에러 응답 객체를 생성하는 정적 팩토리 메소드
     */
    public static ErrorResponse getResponse(ErrorCodeContainer errorCodeContainer) {
        return ErrorResponse.builder()
                .code(errorCodeContainer.getCode())
                .message(errorCodeContainer.getMessage())
                .build();
    }
}
