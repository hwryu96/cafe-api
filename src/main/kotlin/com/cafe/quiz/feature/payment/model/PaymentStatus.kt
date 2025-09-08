package com.cafe.quiz.feature.payment.model

/**
 * 결제 상태
 */
enum class PaymentStatus {
    SUCCESS, // 성공
    FAILURE, // 실패

    ;

    companion object {
        fun from(code: PaymentGatewayResultCode) =
            when (code) {
                PaymentGatewayResultCode.SUCCESS -> SUCCESS
                else -> FAILURE
            }
    }
}
