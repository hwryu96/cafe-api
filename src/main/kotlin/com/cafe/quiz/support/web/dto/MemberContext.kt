package com.cafe.quiz.support.web.dto

/**
 * 로그인 한 회원의 정보,
 * argument resolver를 통해 주입한다.
 */
data class MemberContext(
    val memberId: Long,
)
