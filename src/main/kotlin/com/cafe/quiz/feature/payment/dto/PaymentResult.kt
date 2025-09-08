package com.cafe.quiz.feature.payment.dto

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType

/**
 * 결제 결과
 */
data class PaymentResult(
    val paymentId: Long? = null,
    val paymentType: PaymentType,
    val status: PaymentStatus,
    val resultCode: PaymentGatewayResultCode,
)
