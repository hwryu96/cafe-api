package com.cafe.quiz.feature.payment.postprocessor

import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.entity.PaymentEntity
import com.cafe.quiz.feature.payment.integration.PaymentGateway
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.feature.payment.repository.PaymentEntityRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrNull

/**
 * 대략적인 결제 처리 실패시 플로우를 보인다.
 */
private val log = KotlinLogging.logger {}

@Component
class FailedPaymentPostProcessWorker(
    private val paymentEntityRepository: PaymentEntityRepository,
    private val paymentGateway: PaymentGateway,
) {
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    private val channel = Channel<PaymentResult>(Channel.BUFFERED)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val activated = AtomicBoolean(false)

    init {
        startWorker()
    }

    fun publish(result: PaymentResult) = channel.trySend(result).isSuccess

    private fun startWorker() {
        if (activated.compareAndSet(false, true)) {
            repeat(2) {
                scope.launch {
                    consumeLoop()
                }
            }

            log.info { "결제 후처리 Worker 시작!" }
        }
    }

    private suspend fun consumeLoop() {
        for (result in channel) {
            process(result)
        }
    }

    suspend fun process(result: PaymentResult) {
        val paymentId = result.paymentId ?: return

        val payment =
            paymentEntityRepository.findById(paymentId).getOrNull()
                ?: return

        if (!payment.isRetry()) return

        val result = paymentGateway.refund(payment.pgId)

        // 결제 요청 실패 건은 결제 취소 내역을 생성한다.
        if (payment.type == PaymentType.PAY) {
            paymentEntityRepository.save(
                PaymentEntity(
                    type = PaymentType.REFUND,
                    memberId = payment.memberId,
                    orderId = payment.orderId,
                    price = payment.price,
                    pgId = result.pgId,
                    status = PaymentStatus.from(result.resultCode),
                    resultCode = result.resultCode,
                ),
            )
        }

        if (!result.isSuccessCode()) {
            log.error { "주문번호: ${payment.orderId}, 취소처리 실패! 응답=${result.resultCode}, 수동 확인 필요" }
        }
    }
}
