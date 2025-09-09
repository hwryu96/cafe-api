package com.cafe.quiz.feature.order.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "주문 및 결제 응답")
data class OrderCreateResponse(
    @Schema(title = "주문 번호")
    val orderId: Long,
)
