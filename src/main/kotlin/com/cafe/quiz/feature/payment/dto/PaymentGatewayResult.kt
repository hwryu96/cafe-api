package com.cafe.quiz.feature.payment.dto

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentType

/**
 * 결제 결과
 */
data class PaymentGatewayResult(
    val paymentType: PaymentType,
    val pgId: Long,
    val resultCode: PaymentGatewayResultCode,
)
