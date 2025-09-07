package com.cafe.quiz.feature.member.dto

import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.support.const.PatternConstant
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Schema(title = "회원 생성 요청")
data class MemberRegisterRequest(
    @Schema(title = "이름")
    @field:NotBlank(message = "이름을 입력해주세요.")
    val name: String,
    @Schema(title = "전화번호")
    @field:Pattern(regexp = PatternConstant.PHONE_NUMBER, message = "전화번호 패턴이 유효하지 않아요.")
    val phone: String,
    @Schema(title = "생년월일")
    @field:Pattern(regexp = PatternConstant.BIRTH, message = "생년월일 패턴이 유효하지 않아요.")
    val birth: String,
    @Schema(title = "성별")
    val gender: GenderType,
)
