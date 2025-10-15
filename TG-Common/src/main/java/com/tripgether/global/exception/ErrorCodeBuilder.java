package com.tripgether.global.exception;

import com.tripgether.global.constant.MessageComponent.Action;
import com.tripgether.global.constant.MessageComponent.Status;
import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.util.MessageUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 동적 에러 코드 빌더 클래스
 * ErrorCode 열거형과 동일한 인터페이스 제공하며 Factory 기능 포함
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCodeBuilder {
    private HttpStatus status;
    private String code;
    private String message;

    /**
     * 실패 기반 에러 코드 생성
     */
    public static ErrorCodeBuilder fail(Subject subject, Action action, HttpStatus httpStatus) {
        String code = generateErrorCode(subject, action, httpStatus);
        String message = MessageUtil.failMessage(subject, action);
        return new ErrorCodeBuilder(httpStatus, code, message);
    }

    /**
     * 상태 기반 에러 코드 생성
     */
    public static ErrorCodeBuilder status(Subject subject, Status status, HttpStatus httpStatus) {
        String code = generateErrorCode(subject, status, httpStatus);
        String message = MessageUtil.statusMessage(subject, status);
        return new ErrorCodeBuilder(httpStatus, code, message);
    }

    /**
     * 커스텀 에러 코드 생성
     */
    public static ErrorCodeBuilder custom(HttpStatus httpStatus, String code, String message) {
        return new ErrorCodeBuilder(httpStatus, code, message);
    }

    /**
     * 에러 코드 문자열 생성 메서드
     */
    private static String generateErrorCode(Subject subject, Object action, HttpStatus httpStatus) {
        return subject.name() + "_" + (action != null ? action.toString() : "") + "_" + httpStatus.value();
    }

    /**
     * ErrorCode 열거형과 호환성을 위한 메서드
     * @return 현재 객체 그대로 반환
     */
    public ErrorCodeBuilder getErrorCode() {
        return this;
    }
}
