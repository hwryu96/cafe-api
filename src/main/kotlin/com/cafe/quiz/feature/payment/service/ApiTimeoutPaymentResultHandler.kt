package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.postprocessor.FailedPaymentPostProcessWorker
import org.springframework.stereotype.Component

@Component
class ApiTimeoutPaymentResultHandler(
    private val failedPaymentPostProcessWorker: FailedPaymentPostProcessWorker,
) : PaymentResultHandler {
    override fun support() = PaymentGatewayResultCode.API_TIMEOUT

    override fun payHandle(result: PaymentResult) {
        failedPaymentPostProcessWorker.publish(result)
    }

    override fun refundHandle(result: PaymentResult) {
        failedPaymentPostProcessWorker.publish(result)
    }
}
