package com.tripgether.common.exception;

import com.tripgether.common.exception.constant.ErrorCode;
import lombok.Getter;

/**
 * 커스텀 예외 클래스
 * ErrorCode enum을 사용하여 정의된 에러 코드와 메시지를 포함하는 예외
 */
@Getter
public class CustomException extends RuntimeException {

    private ErrorCodeBuilder errorCodeBuilder;
    private String message;
    private int status;

    /**
     * ErrorCode를 인자로 받는 생성자
     * 에러 코드의 메시지를 상위 클래스의 메시지로 사용
     * @param errorCode 예외와 관련된 에러 코드
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus().value();
    }

    /**
     * ErrorCodeBuilder를 인자로 받는 생성자
     * @param errorCodeBuilder 동적으로 생성된 에러 코드 빌더
     */
    public CustomException(ErrorCodeBuilder errorCodeBuilder) {
        super(errorCodeBuilder.getMessage());
        this.errorCodeBuilder = errorCodeBuilder;
        this.message = errorCodeBuilder.getMessage();
        this.status = errorCodeBuilder.getStatus().value();
    }


    /**
     * HTTP 상태 코드 getter
     * @return HTTP 상태 코드
     */
    public int getStatusCode() {
        return this.status;
    }
}
