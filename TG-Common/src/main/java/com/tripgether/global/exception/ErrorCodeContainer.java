package com.tripgether.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 동적으로 생성된 에러 코드를 담는 컨테이너 클래스
 * ErrorCode 열거형과 동일한 인터페이스 제공
 */
@Getter
public class ErrorCodeContainer {
    private final HttpStatus status;
    private final String code;
    private final String message;

    /**
     * ErrorCodeContainer 생성자
     * @param status HTTP 상태 코드
     * @param code 비즈니스 에러 코드
     * @param message 에러 메시지
     */
    public ErrorCodeContainer(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    /**
     * ErrorCode 열거형과 호환성을 위한 메서드
     * @return 현재 객체 그대로 반환
     */
    public ErrorCodeContainer getErrorCode() {
        return this;
    }
}

