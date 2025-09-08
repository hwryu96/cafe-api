package com.cafe.quiz.feature.payment.model

enum class PaymentGatewayResultCode {
    SUCCESS,
    API_TIMEOUT, // API 타임아웃
    LIMIT_EXCEEDED, // 한도 초과
    ALREADY_PROCESSED, // 이미 처리 된 결제
    REFUNDABLE_PAYMENT_NOT_FOUND, // 환불 가능한 결제 내역 없음
}
