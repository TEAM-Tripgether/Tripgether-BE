package com.tripgether.global.common.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SuccessCodeContainer {
    private final HttpStatus status;
    private final String code;
    private final String message;

    /**
     * SuccessCodeContainer 생성자
     * @param status HTTP 상태 코드
     * @param code 비즈니스 성공 코드
     * @param message 성공 메시지
     */
    public SuccessCodeContainer(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    /**
     * SuccessCode 열거형과 호환성을 위한 메서드
     * @return 현재 객체 그대로 반환
     */
    public SuccessCodeContainer getSuccessCode() {
        return this;
    }
}
