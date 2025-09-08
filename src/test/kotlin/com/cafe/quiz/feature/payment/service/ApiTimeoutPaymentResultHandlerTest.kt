package com.cafe.quiz.feature.payment.service

import com.cafe.quiz.feature.payment.dto.PaymentResult
import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.feature.payment.postprocessor.FailedPaymentPostProcessWorker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ApiTimeoutPaymentResultHandlerTest {
    @Mock
    private lateinit var worker: FailedPaymentPostProcessWorker

    @InjectMocks
    private lateinit var handler: ApiTimeoutPaymentResultHandler

    @Test
    fun `타임아웃이_발생하면_워커에_등록한다`() {
        // given
        val result =
            PaymentResult(
                paymentId = 1L,
                paymentType = PaymentType.PAY,
                status = PaymentStatus.FAILURE,
                resultCode = PaymentGatewayResultCode.API_TIMEOUT,
            )
        whenever(worker.publish(any())).thenReturn(true)

        // when
        handler.payHandle(result)

        // then
        verify(worker).publish(any())
        assertThat(handler.support()).isEqualTo(PaymentGatewayResultCode.API_TIMEOUT)
    }
}
