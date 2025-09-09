package com.cafe.quiz.feature.order.controller

import com.cafe.quiz.feature.order.dto.OrderCreateRequest
import com.cafe.quiz.feature.order.service.OrderService
import com.cafe.quiz.support.web.annotation.ApiController
import com.cafe.quiz.support.web.dto.MemberContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "OrderAPI", description = "주문 API")
@ApiController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @Operation(summary = "주문 > 주문 및 결제")
    @PostMapping
    fun create(
        context: MemberContext,
        @RequestBody param: OrderCreateRequest,
    ) = orderService.create(context.memberId, param.productId)

    @Operation(summary = "주문 > 주문 취소")
    @PostMapping("/{orderId}/cancel")
    fun cancel(
        context: MemberContext,
        @PathVariable("orderId") orderId: Long,
    ) = orderService.cancel(context.memberId, orderId)
}
