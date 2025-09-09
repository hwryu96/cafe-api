package com.cafe.quiz.feature.order.service

import com.cafe.quiz.feature.order.dto.OrderCancelResponse
import com.cafe.quiz.feature.order.dto.OrderCreateResponse
import com.cafe.quiz.feature.order.entity.OrderEntity
import com.cafe.quiz.feature.order.model.OrderStatus
import com.cafe.quiz.feature.order.repository.OrderEntityRepository
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.service.PaymentService
import com.cafe.quiz.support.lock.DistributedLock
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.exception.ApiException
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderEntityRepository: OrderEntityRepository,
    private val paymentService: PaymentService,
    private val distributedLock: DistributedLock,
) {
    /**
     * 실제 주문 프로세스에서는 주문의 준비과 PG사 등 연동을 통한 결제 API를 분리하겠으나
     * 여기서는 주문 준비 과정을 같이 진행하고 바로 결제에 돌입한다.
     */
    fun create(
        memberId: Long,
        productId: Long,
    ) = distributedLock.withLockOrNull("order:$memberId:$productId") {
        // 결제 준비 단계
        val order =
            orderEntityRepository.save(
                OrderEntity(
                    productId = productId,
                    memberId = memberId,
                ),
            )

        // 결제 수행 단계
        val paymentResult = paymentService.pay(memberId, order.orderId, getProductPrice(productId))

        // 수행 결제 상태 반영 단계
        orderEntityRepository.save(
            order.apply {
                status = OrderStatus.of(paymentResult.resultCode, paymentResult.paymentType)
            },
        )

        // 결제 실패 시 실패 응답 반환
        if (order.isFailed()) {
            throwPayFailedException(paymentResult.resultCode)
        }

        OrderCreateResponse(
            orderId = order.orderId,
        )
    } ?: throw ApiException(ApiStatusCode.Conflict.REQUEST_IN_PROGRESS)

    fun cancel(
        memberId: Long,
        orderId: Long,
    ) = distributedLock.withLockOrNull("cancel:$memberId:$orderId") {
        // 취소 주문 조회
        val order =
            orderEntityRepository.findByMemberIdAndOrderId(memberId, orderId)
                ?: throw ApiException(ApiStatusCode.NotFound.ORDER_NOT_FOUND)

        // 취소 가능 여부 검증
        if (!order.isCancelable()) {
            throw ApiException(ApiStatusCode.Conflict.CANCEL_NOT_ALLOWED)
        }

        // 결제 취소 요청
        // 해당 결제 취소는 실제 결제 취소에 실패해도, 추후 후처리 혹은 수동으로 재처리 된다고 가정한다.
        // 부분 취소나 다른 형태는 고려하지 않는다.
        paymentService.refund(orderId)
        orderEntityRepository.save(
            order.apply {
                status = OrderStatus.CANCELLED
            },
        )

        OrderCancelResponse(
            orderId = order.orderId,
        )
    } ?: throw ApiException(ApiStatusCode.Conflict.REQUEST_IN_PROGRESS)

    private fun getProductPrice(productId: Long): Long = productId * 1000

    private fun throwPayFailedException(code: PaymentGatewayResultCode): Nothing {
        when (code) {
            PaymentGatewayResultCode.LIMIT_EXCEEDED -> throw ApiException(ApiStatusCode.Conflict.LIMIT_EXCEEDED)
            PaymentGatewayResultCode.REFUNDABLE_PAYMENT_NOT_FOUND -> throw ApiException(ApiStatusCode.Conflict.REFUNDABLE_PAYMENT_NOT_FOUND)
            else -> throw ApiException(ApiStatusCode.Conflict.ORDER_FAILED)
        }
    }
}
