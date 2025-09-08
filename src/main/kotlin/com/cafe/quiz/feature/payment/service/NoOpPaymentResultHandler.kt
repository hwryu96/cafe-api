package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import org.springframework.stereotype.Component

@Component
class NoOpPaymentResultHandler : PaymentResultHandler {
    override fun support() = PaymentGatewayResultCode.SUCCESS
}
