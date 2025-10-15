package com.tripgether.global.exception;

import com.tripgether.global.constant.ErrorCodeContainer;
import lombok.Getter;

/**
 * 커스텀 예외 클래스
 * ErrorCode enum을 사용하여 정의된 에러 코드와 메시지를 포함하는 예외
 */
@Getter
public class CustomException extends RuntimeException {

    // 필요하면 나중에 ErrorCode enum도 변경
    //  private ErrorCode errorCode; // 예외와 관련된 에러 코드
    private ErrorCodeContainer errorCodeContainer;
    private String code;
    private String message;
    private int status;

    /**
     * ErrorCode를 인자로 받는 생성자
     * 에러 코드의 메시지를 상위 클래스의 메시지로 사용
     * @param errorCode 예외와 관련된 에러 코드
     */
//    public CustomException(ErrorCode errorCode) {
//        super(errorCode.getMessage());
//        this.errorCode = errorCode;
//    }

    /**
     * ErrorCodeContainer를 인자로 받는 생성자
     * @param errorCodeContainer 동적으로 생성된 에러 코드 컨테이너
     */
    public CustomException(ErrorCodeContainer errorCodeContainer) {
        super(errorCodeContainer.getMessage());
        this.errorCodeContainer = errorCodeContainer;
        this.code = errorCodeContainer.getCode();
        this.message = errorCodeContainer.getMessage();
        this.status = errorCodeContainer.getStatus().value();
    }

    /**
     * 에러 코드 getter - 호환성 유지
     * @return 에러 코드
     */
    public String getErrorCodeString() {
        return this.code;
    }

    /**
     * HTTP 상태 코드 getter
     * @return HTTP 상태 코드
     */
    public int getStatusCode() {
        return this.status;
    }
}
