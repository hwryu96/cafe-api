package com.cafe.quiz.feature.member.controller

import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.service.MemberService
import com.cafe.quiz.support.web.annotation.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "MemberAPI", description = "회원 API")
@ApiController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {
    @Operation(summary = "회원 > 회원 가입")
    @PostMapping
    fun register(
        @Valid @RequestBody request: MemberRegisterRequest,
    ) = memberService.register(request)
}
