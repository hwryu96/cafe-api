package com.cafe.quiz.feature.order.model

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentType

enum class OrderStatus {
    READY, // 대기중
    PAID, // 결제 완료
    CANCELLED, // 사용자 요청에 의해 취소 됨
    FAILED, // 내부 사유로 주문이 실패함
    ;

    companion object {
        fun of(
            code: PaymentGatewayResultCode,
            type: PaymentType,
        ): OrderStatus =
            when (code) {
                PaymentGatewayResultCode.SUCCESS -> if (type == PaymentType.PAY) PAID else CANCELLED
                else -> FAILED
            }
    }
}
