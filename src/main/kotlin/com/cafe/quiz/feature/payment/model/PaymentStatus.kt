package com.cafe.quiz.feature.payment.model

/**
 * 결제 상태
 */
enum class PaymentStatus {
    SUCCESS, // 성공
    FAILURE, // 실패
    TIMEOUT, // 타임아웃
    // 추후 필요한 결제 상태 코드가 있으면 다음을 확장
}
