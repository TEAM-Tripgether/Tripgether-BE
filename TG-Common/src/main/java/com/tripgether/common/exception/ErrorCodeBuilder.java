package com.tripgether.common.exception;

import com.tripgether.common.exception.constant.ErrorMessageTemplate.Action;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Status;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Subject;
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
        String message = failMessage(subject, action);
        return new ErrorCodeBuilder(httpStatus, code, message);
    }

    /**
     * 상태 기반 에러 코드 생성
     */
    public static ErrorCodeBuilder status(Subject subject, Status status, HttpStatus httpStatus) {
        String code = generateErrorCode(subject, status, httpStatus);
        String message = statusMessage(subject, status);
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

    /**
     * 실패 메시지 생성
     * 예: "공통 코드 그룹 생성에 실패했습니다."
     *
     * @param subject 메시지 주체 (명사)
     * @param action 메시지 행위 (동사)
     * @return 생성된 실패 메시지
     */
    public static String failMessage(Subject subject, Action action) {
        return String.format("%s %s에 실패했습니다.", subject.getValue(), action.getValue());
    }

    /**
     * 상태별 메시지 생성
     * 예: "공통 코드 그룹을 찾을 수 없습니다."
     *
     * @param subject 메시지 주체 (명사)
     * @param status 메시지 상태
     * @return 생성된 상태 메시지
     */
    public static String statusMessage(Subject subject, Status status) {
        switch (status) {
            case NOT_FOUND:
                return String.format("%s을(를) 찾을 수 없습니다.", subject.getValue());
            case DUPLICATE:
                return String.format("이미 존재하는 %s입니다.", subject.getValue());
            case INVALID:
                return String.format("유효하지 않은 %s입니다.", subject.getValue());
            default:
                return String.format("%s이(가) %s 상태입니다.", subject.getValue(), status.getValue());
        }
    }

    /**
     * 사용자 정의 메시지 생성
     * 예: "공통 코드 그룹 생성 중 오류: 유효하지 않은 입력값"
     *
     * @param subject 메시지 주체 (명사)
     * @param action 메시지 행위 (동사)
     * @param detail 세부 메시지
     * @return 생성된 사용자 정의 메시지
     */
    public static String customMessage(Subject subject, Action action, String detail) {
        return String.format("%s %s 중 오류: %s", subject.getValue(), action.getValue(), detail);
    }
}
