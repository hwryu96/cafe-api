package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.dto.PaymentGatewayResult
import com.cafe.quiz.feature.payment.entity.PaymentEntity
import com.cafe.quiz.feature.payment.integration.PaymentGateway
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.feature.payment.repository.PaymentEntityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {
    @Mock
    private lateinit var repo: PaymentEntityRepository

    @Mock
    private lateinit var gateway: PaymentGateway

    @Mock
    private lateinit var dispatcher: PaymentGatewayResultHandlerDispatcher

    @Mock
    private lateinit var handler: PaymentResultHandler

    @InjectMocks
    private lateinit var service: PaymentService

    @Test
    fun `결제가_성공한다`() {
        // given
        whenever(gateway.pay(10_000L)).thenReturn(
            PaymentGatewayResult(
                paymentType = PaymentType.PAY,
                pgId = 123L,
                resultCode = PaymentGatewayResultCode.SUCCESS,
            ),
        )

        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(dispatcher.dispatch(PaymentGatewayResultCode.SUCCESS)).thenReturn(handler)

        // when
        val result = service.pay(memberId = 1L, orderId = 2L, price = 10_000L)

        // then
        assertThat(result.status).isEqualTo(PaymentStatus.SUCCESS)
        assertThat(result.resultCode).isEqualTo(PaymentGatewayResultCode.SUCCESS)
        assertThat(result.paymentType).isEqualTo(PaymentType.PAY)

        verify(gateway).pay(10_000L)
        verify(repo).save(
            check {
                assertThat(it.memberId).isEqualTo(1L)
                assertThat(it.orderId).isEqualTo(2L)
                assertThat(it.price).isEqualTo(10_000L)
                assertThat(it.pgId).isEqualTo(123L)
                assertThat(it.status).isEqualTo(PaymentStatus.SUCCESS)
                assertThat(it.resultCode).isEqualTo(PaymentGatewayResultCode.SUCCESS)
            },
        )
    }

    @Test
    fun `결제시_재처리가_필요한_경우에는_재처리가_등록된다`() {
        // given
        whenever(gateway.pay(10_000L)).thenReturn(
            PaymentGatewayResult(
                paymentType = PaymentType.PAY,
                pgId = 555L,
                resultCode = PaymentGatewayResultCode.API_TIMEOUT,
            ),
        )

        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(dispatcher.dispatch(PaymentGatewayResultCode.API_TIMEOUT)).thenReturn(handler)

        // when
        val result = service.pay(memberId = 9L, orderId = 99L, price = 10_000L)

        // then
        assertThat(result.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(result.resultCode).isEqualTo(PaymentGatewayResultCode.API_TIMEOUT)

        verify(gateway).pay(10_000L)
        verify(dispatcher).dispatch(PaymentGatewayResultCode.API_TIMEOUT)
        verify(handler).payHandle(any())
    }

    @Test
    fun `환불시_환불_가능한_결제가_없으면_실패를_응답한다`() {
        // given
        whenever(repo.findByOrderId(777L)).thenReturn(emptyList())

        // when
        val result = service.refund(777L)

        // then
        assertThat(result.paymentType).isEqualTo(PaymentType.REFUND)
        assertThat(result.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(result.resultCode).isEqualTo(PaymentGatewayResultCode.REFUNDABLE_PAYMENT_NOT_FOUND)
    }

    @Test
    fun `환불이_성공한다`() {
        // given
        val active =
            PaymentEntity(
                memberId = 10L,
                orderId = 888L,
                price = 12_345L,
                pgId = 321L,
                status = PaymentStatus.SUCCESS,
                resultCode = PaymentGatewayResultCode.SUCCESS,
            )
        whenever(repo.findByOrderId(888L)).thenReturn(listOf(active))
        whenever(gateway.refund(321L)).thenReturn(
            PaymentGatewayResult(
                paymentType = PaymentType.REFUND,
                pgId = 999L,
                resultCode = PaymentGatewayResultCode.SUCCESS,
            ),
        )
        whenever(dispatcher.dispatch(PaymentGatewayResultCode.SUCCESS)).thenReturn(handler)
        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        // when
        val result = service.refund(888L)

        // then
        assertThat(result.paymentType).isEqualTo(PaymentType.REFUND)
        assertThat(result.status).isEqualTo(PaymentStatus.SUCCESS)

        verify(gateway).refund(321L)
        verify(dispatcher).dispatch(PaymentGatewayResultCode.SUCCESS)
        verify(handler).refundHandle(any())
    }
}
