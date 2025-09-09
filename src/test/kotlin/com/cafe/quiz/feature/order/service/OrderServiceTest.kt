package com.cafe.quiz.feature.order.service

import com.cafe.quiz.feature.order.dto.OrderCancelResponse
import com.cafe.quiz.feature.order.entity.OrderEntity
import com.cafe.quiz.feature.order.model.OrderStatus
import com.cafe.quiz.feature.order.repository.OrderEntityRepository
import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.feature.payment.service.PaymentService
import com.cafe.quiz.support.lock.DistributedLock
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.exception.ApiException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.capture
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {
    @Mock
    private lateinit var repo: OrderEntityRepository

    @Mock
    private lateinit var paymentService: PaymentService

    @Mock
    private lateinit var lock: DistributedLock

    @InjectMocks
    private lateinit var service: OrderService

    @Captor
    lateinit var orderEntityCaptor: ArgumentCaptor<OrderEntity>

    private val memberId = 11L
    private val productId = 77L

    @Test
    fun `주문에_성공한다`() {
        // given
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }
        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(
            paymentService.pay(eq(memberId), any(), eq(productId * 1000)),
        ).thenReturn(
            PaymentResult(
                paymentType = PaymentType.PAY,
                status = PaymentStatus.SUCCESS,
                resultCode = PaymentGatewayResultCode.SUCCESS,
            ),
        )

        // when
        val res = service.create(memberId, productId)

        // then
        verify(repo, times(2)).save(any())
        verify(repo, atLeastOnce()).save(capture(orderEntityCaptor))
        val saved = orderEntityCaptor.value

        assertThat(saved.productId).isEqualTo(productId)
        assertThat(saved.memberId).isEqualTo(memberId)
        assertThat(saved.orderId).isEqualTo(res.orderId)
    }

    @Test
    fun `주문시_타임아웃에_의한_결제_실패는_예외가_발생한다`() {
        // given
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }
        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(
            paymentService.pay(eq(memberId), any(), eq(productId * 1000)),
        ).thenReturn(
            PaymentResult(
                paymentType = PaymentType.PAY,
                status = PaymentStatus.FAILURE,
                resultCode = PaymentGatewayResultCode.API_TIMEOUT,
            ),
        )

        // when & then
        assertThatThrownBy { service.create(memberId, productId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.Conflict.ORDER_FAILED }
    }

    @Test
    fun `주문시_한도초과에_의한_결제_실패는_예외가_발생한다`() {
        // given
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }
        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(
            paymentService.pay(eq(memberId), any(), eq(productId * 1000)),
        ).thenReturn(
            PaymentResult(
                paymentType = PaymentType.PAY,
                status = PaymentStatus.FAILURE,
                resultCode = PaymentGatewayResultCode.LIMIT_EXCEEDED,
            ),
        )

        // when & then
        assertThatThrownBy { service.create(memberId, productId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.Conflict.LIMIT_EXCEEDED }
    }

    @Test
    fun `주문시_락_경합이_발생하면_예외가_발생한다`() {
        // given
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenReturn(null)

        // when & then
        assertThatThrownBy { service.create(memberId, productId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.Conflict.REQUEST_IN_PROGRESS }

        verify(lock, never()).unlock(any())
    }

    @Test
    fun `주문_취소에_성공한다`() {
        // given
        val orderId = 555L
        val order = OrderEntity(orderId = orderId, memberId = memberId, productId = productId, status = OrderStatus.PAID)
        whenever(repo.findByMemberIdAndOrderId(memberId, orderId)).thenReturn(order)
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }
        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        whenever(paymentService.refund(orderId)).thenReturn(
            PaymentResult(
                paymentType = PaymentType.REFUND,
                status = PaymentStatus.SUCCESS,
                resultCode = PaymentGatewayResultCode.SUCCESS,
            ),
        )

        // when
        val res: OrderCancelResponse = service.cancel(memberId, orderId)

        // then
        assertThat(order.status).isEqualTo(OrderStatus.CANCELLED)
        assertThat(res.orderId).isEqualTo(orderId)
    }

    @Test
    fun `주문_취소시_취소_불가능한_주문은_예외가_발생한다`() {
        // given
        val orderId = 999L
        val order = OrderEntity(orderId = orderId, memberId = memberId, productId = productId, status = OrderStatus.READY)
        whenever(repo.findByMemberIdAndOrderId(memberId, orderId)).thenReturn(order)
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }

        // when & then
        assertThatThrownBy { service.cancel(memberId, orderId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.Conflict.CANCEL_NOT_ALLOWED }

        verify(paymentService, never()).refund(any())
        verify(repo, never()).save(any())
    }

    @Test
    fun `주문_취소시_취소_대상이_없으면_예외가_발생한다`() {
        // given
        val orderId = 404L
        whenever(repo.findByMemberIdAndOrderId(memberId, orderId)).thenReturn(null)
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenAnswer { inv ->
            (inv.arguments[2] as () -> Any?).invoke()
        }

        // when & then
        assertThatThrownBy { service.cancel(memberId, orderId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.NotFound.ORDER_NOT_FOUND }

        verify(paymentService, never()).refund(any())
        verify(repo, never()).save(any())
    }

    @Test
    fun `주문_취소시_락_경합이_발생하면_예외가_발생한다`() {
        // given
        val orderId = 1L
        whenever(lock.withLockOrNull<Any?>(any(), any(), any())).thenReturn(null)

        // when & then
        assertThatThrownBy { service.cancel(memberId, orderId) }
            .isInstanceOf(ApiException::class.java)
            .matches { (it as ApiException).code == ApiStatusCode.Conflict.REQUEST_IN_PROGRESS }
    }
}
