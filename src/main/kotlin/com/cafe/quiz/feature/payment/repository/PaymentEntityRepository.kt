package com.cafe.quiz.feature.payment.repository

import com.cafe.quiz.feature.payment.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentEntityRepository : JpaRepository<PaymentEntity, Long>
