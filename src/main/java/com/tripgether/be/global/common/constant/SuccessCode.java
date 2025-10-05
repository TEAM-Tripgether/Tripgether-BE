package com.tripgether.be.global.common.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum SuccessCode {

    // Global
    _OK(HttpStatus.OK, "COMMON200", "요청이 성공적으로 처리되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    SuccessCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
