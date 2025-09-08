package com.cafe.quiz.feature.payment.integration

import com.cafe.quiz.feature.payment.dto.PaymentGatewayResult

interface PaymentGateway {
    fun pay(price: Long): PaymentGatewayResult

    fun refund(pgId: Long): PaymentGatewayResult
}
