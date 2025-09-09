package com.cafe.quiz.feature.order.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "취소 응답")
data class OrderCancelResponse(
    @Schema(title = "주문 번호")
    val orderId: Long,
)
