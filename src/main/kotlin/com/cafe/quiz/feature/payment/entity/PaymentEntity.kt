package com.cafe.quiz.feature.payment.entity

import com.cafe.quiz.feature.payment.model.PaymentGatewayResultCode
import com.cafe.quiz.feature.payment.model.PaymentStatus
import com.cafe.quiz.feature.payment.model.PaymentType
import com.cafe.quiz.support.generator.IdGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    val id: Long = IdGenerator.generate(),
    @Column(name = "order_id", nullable = false)
    val orderId: Long,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(name = "price", nullable = false)
    val price: Long,
    @Column(name = "pg_id", nullable = false)
    var pgId: Long,
    /**
     * 결제의 구분
     * 결제 요청과 취소 요청을 구분한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 10, nullable = false)
    val type: PaymentType = PaymentType.PAY,
    /**
     * 결제의 상태
     * 성공과 실패를 파악한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 10, nullable = false)
    val status: PaymentStatus,
    /**
     * 결제 응답 코드
     * 결제 응답 코드는 래핑되어 PaymentResultCode로 반환한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result_code", length = 10, nullable = false)
    val resultCode: PaymentGatewayResultCode,
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isRetry() = resultCode == PaymentGatewayResultCode.API_TIMEOUT
}
