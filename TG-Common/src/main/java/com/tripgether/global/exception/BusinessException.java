package com.tripgether.global.exception;

import com.tripgether.global.constant.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 예외를 표현하는 클래스
 * 비즈니스 규칙 위반 시 사용
 */
@Getter
public class BusinessException extends RuntimeException {

     private final ErrorCode errorCode; // 예외와 관련된 에러 코드

    /**
     * 메시지와 에러 코드를 인자로 받는 생성자
     * @param message 사용자 정의 에러 메시지
     * @param errorCode 예외와 관련된 에러 코드
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드만 인자로 받는 생성자
     * 에러 코드의 기본 메시지 사용
     * @param errorCode 예외와 관련된 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드와 추가 메시지를 결합하여 사용하는 생성자
     * @param errorCode 예외와 관련된 에러 코드
     * @param additionalMessage 기본 에러 메시지에 추가할 메시지
     */
    public BusinessException(ErrorCode errorCode, String additionalMessage) {
        super(errorCode.getMessage() + " - " + additionalMessage);
        this.errorCode = errorCode;
    }
}
