package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class PaymentGatewayResultHandlerDispatcherTest {
    @Test
    fun `매핑된_핸들러가_없으면_기본_핸들러를_반환한다`() {
        val default = NoOpPaymentResultHandler()
        val dispatcher = PaymentGatewayResultHandlerDispatcher(default, handlers = listOf(default))

        val handler = dispatcher.dispatch(PaymentGatewayResultCode.LIMIT_EXCEEDED)
        assertThat(handler).isInstanceOf(NoOpPaymentResultHandler::class.java)
    }

    @Test
    fun `등록된_핸들러가_있으면_등록된_핸들러를_반환한다`() {
        val default = NoOpPaymentResultHandler()
        val apiTimeout = ApiTimeoutPaymentResultHandler(mock()) // worker mock
        val dispatcher = PaymentGatewayResultHandlerDispatcher(default, handlers = listOf(default, apiTimeout))

        val handler = dispatcher.dispatch(PaymentGatewayResultCode.API_TIMEOUT)
        assertThat(handler).isInstanceOf(ApiTimeoutPaymentResultHandler::class.java)
    }
}
