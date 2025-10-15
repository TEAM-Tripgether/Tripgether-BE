package com.tripgether.global.util;

import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.constant.MessageComponent.Action;
import com.tripgether.global.constant.MessageComponent.Status;

/**
 * 메시지 생성 유틸리티 클래스
 */
public class MessageUtil {

    /**
     * 성공 메시지 생성
     * 예시 : "공통 코드 그룹이 성공적으로 생성되었습니다."
     *
     * @param subject 메시지의 주체
     * @param action 메시지의 행위
     * @return 생성된 성공 메시지
     */
    public static String successMessage(Subject subject, Action action) {
        return String.format("%s이(가) 성공적으로 %s되었습니다.", subject.getValue(), action.getValue());
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
