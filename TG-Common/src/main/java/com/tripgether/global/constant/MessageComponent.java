package com.tripgether.global.constant;

/**
 * 메시지 구성 요소들을 정의하는 클래스
 */
public class MessageComponent {

    /**
     * 메시지의 주체(명사) 정의
     */
    public enum Subject {
        EXAMPLE("예시"),
        USER("사용자"),
        AUTH("인증");

        private final String value;

        Subject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 메시지의 행위(동사) 정의
     */
    public enum Action {
        CREATE("생성"),
        UPDATE("수정"),
        DELETE("삭제"),
        FIND("조회");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 메시지의 결과 상태 정의
     */
    public enum Status {
        SUCCESS("성공"),
        FAIL("실패"),
        NOT_FOUND("찾을 수 없음"),
        DUPLICATE("중복"),
        INVALID("유효하지 않음"),
        EXPIRED("만료됨"),
        DENIED("거부됨");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
