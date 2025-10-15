package com.tripgether.global.constant;

import com.tripgether.global.util.MessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.constant.MessageComponent.Action;
import com.tripgether.global.constant.MessageComponent.Status;

/**
 * 동적 에러 코드 생성기
 */
@Component
public class ErrorCodeFactory {

    // 에러 코드 문자열 생성 메서드
    private static String generateErrorCode(Subject subject, Object action, HttpStatus httpStatus) {
        return subject.name() + "_" + (action != null ? action.toString() : "") + "_" + httpStatus.value();
    }

    /**
     * 실패 기반 에러 코드 생성
     */
    public static ErrorCodeContainer fail(Subject subject, Action action, HttpStatus httpStatus) {
        String code = generateErrorCode(subject, action, httpStatus);
        String message = MessageUtil.failMessage(subject, action);
        return new ErrorCodeContainer(httpStatus, code, message);
    }

    /**
     * 상태 기반 에러 코드 생성
     */
    public static ErrorCodeContainer status(Subject subject, Status status, HttpStatus httpstatus) {
        String code = generateErrorCode(subject, status, httpstatus);
        String message = MessageUtil.statusMessage(subject, status);
        return new ErrorCodeContainer(httpstatus, code, message);
    }

    /**
     * 커스텀 에러 코드 생성
     */
    public static ErrorCodeContainer custom(HttpStatus httpStatus, String code, String message) {
        return new ErrorCodeContainer(httpStatus, code, message);
    }

}
