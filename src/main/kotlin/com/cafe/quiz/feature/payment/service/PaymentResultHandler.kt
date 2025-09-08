package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode

interface PaymentResultHandler {
    fun support(): PaymentGatewayResultCode

    fun payHandle(result: PaymentResult) { }

    fun refundHandle(result: PaymentResult) { }
}
