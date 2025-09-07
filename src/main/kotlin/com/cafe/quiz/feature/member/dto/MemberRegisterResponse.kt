package com.cafe.quiz.feature.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "회원 생성 응답")
data class MemberRegisterResponse(
    @Schema(title = "생성 된 회원의 ID")
    val id: Long,
)
