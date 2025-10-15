package com.tripgether.global.exception;

import com.tripgether.global.constant.SuccessCode;
import com.tripgether.global.constant.SuccessCodeContainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * API 성공 응답을 표현하는 클래스
 * 응답 코드, 메시지, 데이터를 포함
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class SuccessResponse<T> {

    private String code;    // 성공 코드
    private String message; // 성공 메시지
    private T data;         // 응답 데이터 (제네릭 타입)

    /**
     * SuccessCode를 사용하여 성공 응답 생성
     * @param <T> 응답 데이터의 타입
     * @param successCode 성공 코드 enum
     * @param data 응답에 포함될 데이터
     * @return 성공 응답 객체
     */
    public static <T> SuccessResponse<T> getResponse(SuccessCode successCode, T data) {
        return SuccessResponse.<T>builder()
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    /**
     * SuccessCodeContainer를 사용하여 성공 응답 생성
     * @param <T> 응답 데이터의 타입
     * @param successCode 성공 코드 컨테이너
     * @param data 응답에 포함될 데이터
     * @return 성공 응답 객체
     */
    public static <T> SuccessResponse<T> getResponse(SuccessCodeContainer successCode, T data) {
        return SuccessResponse.<T>builder()
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    /**
     * 데이터 없이 성공 코드와 메시지만 포함한 응답을 생성하는 정적 팩토리 메소드
     * @param successCode 성공 코드 enum
     * @return 데이터가 null인 성공 응답 객체
     */
    public static SuccessResponse<Void> getResponse(SuccessCode successCode) {
        return SuccessResponse.<Void>builder()
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .data(null)
                .build();
    }

    /**
     * 데이터 없이 성공 코드 컨테이너만으로 응답 생성
     * @param successCode 성공 코드 컨테이너
     * @return 데이터가 null인 성공 응답 객체
     */
    public static SuccessResponse<Void> getResponse(SuccessCodeContainer successCode) {
        return SuccessResponse.<Void>builder()
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .data(null)
                .build();
    }
}