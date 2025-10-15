package com.tripgether.global.exception.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버에 문제가 발생했습니다."),

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON400", "유효하지 않은 입력값입니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON403", "접근이 거부되었습니다"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "지원하지 않는 HTTP 메소드입니다."),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON415", "지원하지 않는 미디어 타입입니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500-DB", "데이터베이스 오류가 발생했습니다."),


    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH401", "인증이 필요합니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-TOKEN", "유효하지 않은 토큰입니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401-TOKEN-EXP", "만료된 토큰입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}

