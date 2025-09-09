package com.cafe.quiz.feature.order.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "주문 및 결제 요청")
data class OrderCreateRequest(
    @Schema(title = "상품 번호")
    val productId: Long,
)
