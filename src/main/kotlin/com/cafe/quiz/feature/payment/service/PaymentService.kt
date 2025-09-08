package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.entity.PaymentEntity
import com.cafe.quiz.feature.payment.integration.PaymentGateway
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.feature.payment.repository.PaymentEntityRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentEntityRepository: PaymentEntityRepository,
    private val paymentGateway: PaymentGateway,
    private val paymentGatewayResultHandlerDispatcher: PaymentGatewayResultHandlerDispatcher,
) {
    /**
     * 결제 요청,
     * 일반적으로 결제 준비 과정 -> 결제 요청 과정을 진행하겠으나
     * 결제 준비 과정이 의미가 없으므로 제외한다.
     */
    fun pay(
        memberId: Long,
        orderId: Long,
        price: Long,
    ): PaymentResult {
        // 결제를 처리한다
        val gatewayResult = paymentGateway.pay(price)

        // 데이터를 저장한다
        val saved =
            paymentEntityRepository.save(
                PaymentEntity(
                    memberId = memberId,
                    orderId = orderId,
                    price = price,
                    pgId = gatewayResult.pgId,
                    status = PaymentStatus.from(gatewayResult.resultCode),
                    resultCode = gatewayResult.resultCode,
                ),
            )

        val paymentResult =
            PaymentResult(
                paymentId = saved.id,
                status = saved.status,
                resultCode = saved.resultCode,
                paymentType = saved.type,
            )

        // 각 핸들러에 위임하여 상태별 후처리를 진행한다
        paymentGatewayResultHandlerDispatcher
            .dispatch(gatewayResult.resultCode)
            .payHandle(paymentResult)

        return paymentResult
    }

    fun refund(orderId: Long): PaymentResult {
        // 유효한 결제 정보를 조회한다
        val payments = paymentEntityRepository.findByOrderId(orderId)
        val activePayment =
            payments.firstOrNull { it.type == PaymentType.PAY && it.status == PaymentStatus.SUCCESS }
                ?: return PaymentResult(
                    status = PaymentStatus.FAILURE,
                    resultCode = PaymentGatewayResultCode.REFUNDABLE_PAYMENT_NOT_FOUND,
                    paymentType = PaymentType.REFUND,
                )

        // 환불을 처리한다
        val gatewayResult = paymentGateway.refund(activePayment.pgId)

        // 데이터를 저장한다.
        val saved =
            paymentEntityRepository.save(
                PaymentEntity(
                    type = PaymentType.REFUND,
                    memberId = activePayment.memberId,
                    orderId = activePayment.orderId,
                    price = activePayment.price,
                    pgId = gatewayResult.pgId,
                    status = PaymentStatus.from(gatewayResult.resultCode),
                    resultCode = gatewayResult.resultCode,
                ),
            )

        val paymentResult =
            PaymentResult(
                paymentId = saved.id,
                status = saved.status,
                resultCode = saved.resultCode,
                paymentType = saved.type,
            )

        // 각 핸들러에 위임하여 상태별 후처리를 진행한다
        paymentGatewayResultHandlerDispatcher
            .dispatch(gatewayResult.resultCode)
            .refundHandle(paymentResult)

        return paymentResult
    }
}
