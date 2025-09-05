package com.cafe.quiz.support.web.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "API 공통 응답")
data class ApiResponse<T>(
    @Schema(title = "코드")
    val code: String = "",
    @Schema(title = "메시지")
    val message: String? = null,
    @Schema(title = "데이터")
    val data: T? = null,
) {
    companion object {
        fun from(status: StatusCode): ApiResponse<Any> =
            ApiResponse(
                status.code,
                status.message,
            )
    }
}
