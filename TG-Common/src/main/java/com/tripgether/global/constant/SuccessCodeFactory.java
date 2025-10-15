package com.tripgether.global.constant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.tripgether.global.util.MessageUtil;
import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.constant.MessageComponent.Action;


@Component
public class SuccessCodeFactory {

    // 성공 코드 문자열 생성 메서드
    private static String generateSuccessCode(Subject subject, Action action, HttpStatus httpStatus) {
        return subject.name() + "_" + (action != null ? action.toString() : "") + "_" + httpStatus.value();
    }

    /**
     * 성공 기반 코드 생성
     */
    public static SuccessCodeContainer success(Subject subject, Action action, HttpStatus httpStatus) {
        String code = generateSuccessCode(subject, action, httpStatus);
        String message = MessageUtil.successMessage(subject, action);
        return new SuccessCodeContainer(httpStatus, code, message);
    }

    /**
     * 생성 성공 코드 생성 (HTTP 201)
     */
    public static SuccessCodeContainer created(Subject subject) {
        String code = generateSuccessCode(subject, Action.CREATE, HttpStatus.CREATED);
        String message = MessageUtil.successMessage(subject, Action.CREATE);
        return new SuccessCodeContainer(HttpStatus.CREATED, code, message);
    }

    /**
     * 수정 성공 코드 생성 (HTTP 200)
     */
    public static SuccessCodeContainer updated(Subject subject) {
        String code = generateSuccessCode(subject, Action.UPDATE, HttpStatus.OK);
        String message = MessageUtil.successMessage(subject, Action.UPDATE);
        return new SuccessCodeContainer(HttpStatus.OK, code, message);
    }

    /**
     * 삭제 성공 코드 생성 (HTTP 200)
     */
    public static SuccessCodeContainer deleted(Subject subject) {
        String code = generateSuccessCode(subject, Action.DELETE, HttpStatus.OK);
        String message = MessageUtil.successMessage(subject, Action.DELETE);
        return new SuccessCodeContainer(HttpStatus.OK, code, message);
    }

    /**
     * 조회 성공 코드 생성 (HTTP 200)
     */
    public static SuccessCodeContainer retrieved(Subject subject) {
        String code = generateSuccessCode(subject, Action.FIND, HttpStatus.OK);
        String message = MessageUtil.successMessage(subject, Action.FIND);
        return new SuccessCodeContainer(HttpStatus.OK, code, message);
    }

    /**
     * 커스텀 성공 코드 생성
     */
    public static SuccessCodeContainer custom(HttpStatus httpStatus, String code, String message) {
        return new SuccessCodeContainer(httpStatus, code, message);
    }
}
