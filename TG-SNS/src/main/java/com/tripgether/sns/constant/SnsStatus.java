package com.tripgether.sns.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnsStatus {
    PENDING,    // 분석 대기 중
    ANALYZING,  // 분석 중
    COMPLETED,  // 분석 완료
    FAILED,     // 분석 실패
    DELETED     // 삭제됨
}
