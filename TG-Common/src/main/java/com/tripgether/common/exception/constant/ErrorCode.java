package com.tripgether.common.exception.constant;

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

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH401-CRED", "아이디 또는 비밀번호가 올바르지 않습니다."),

    MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-MISSING", "인증 토큰이 필요합니다."),

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-ACCESS", "유효하지 않은 액세스 토큰입니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-REFRESH", "유효하지 않은 리프레시 토큰입니다."),

    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-ACCESS-EXP", "액세스 토큰이 만료되었습니다."),

    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-REFRESH-EXP", "리프레시 토큰이 만료되었습니다."),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH404-REFRESH", "리프레시 토큰을 찾을 수 없습니다."),

    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "AUTH401-BLACKLIST", "블랙리스트 처리된 토큰입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-TOKEN", "유효하지 않은 토큰입니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401-TOKEN-EXP", "만료된 토큰입니다."),


    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "회원을 찾을 수 없습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER400-EMAIL", "이미 가입된 이메일입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}

