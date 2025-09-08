package com.cafe.quiz.feature.payment.integration

import com.cafe.quiz.feature.payment.dto.PaymentGatewayResult
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.support.generator.IdGenerator
import org.springframework.stereotype.Component
import java.security.SecureRandom

/**
 * PG사의 기능을 대신하기 위해 모킹한 클래스
 */
@Component
class FakePaymentGateway : PaymentGateway {
    private val random = SecureRandom()

    override fun pay(price: Long) = makeFakeResult()

    override fun refund(pgId: Long) = makeFakeResult(pgId)

    private fun makeFakeResult(pgId: Long? = null): PaymentGatewayResult {
        val delay = random.nextInt(1000)
        val paymentType = if (pgId == null) PaymentType.PAY else PaymentType.REFUND
        Thread.sleep(delay.toLong())

        return when {
            (delay > 700) ->
                PaymentGatewayResult(
                    pgId = pgId ?: IdGenerator.generate(),
                    resultCode = PaymentGatewayResultCode.API_TIMEOUT,
                    paymentType = paymentType,
                )
            (delay % 5 == 0) ->
                PaymentGatewayResult(
                    pgId = pgId ?: IdGenerator.generate(),
                    resultCode = PaymentGatewayResultCode.ALREADY_PROCESSED,
                    paymentType = paymentType,
                )
            (delay % 3 == 0) ->
                PaymentGatewayResult(
                    pgId = pgId ?: IdGenerator.generate(),
                    resultCode = PaymentGatewayResultCode.LIMIT_EXCEEDED,
                    paymentType = paymentType,
                )
            else ->
                PaymentGatewayResult(
                    pgId = pgId ?: IdGenerator.generate(),
                    resultCode = PaymentGatewayResultCode.SUCCESS,
                    paymentType = paymentType,
                )
        }
    }
}
