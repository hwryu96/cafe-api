package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import org.springframework.stereotype.Component

@Component
class PaymentGatewayResultHandlerDispatcher(
    private val default: NoOpPaymentResultHandler,
    handlers: List<PaymentResultHandler>,
) {
    private val handlerMap = handlers.associateBy { it.support() }

    fun dispatch(code: PaymentGatewayResultCode) = handlerMap[code] ?: default
}
