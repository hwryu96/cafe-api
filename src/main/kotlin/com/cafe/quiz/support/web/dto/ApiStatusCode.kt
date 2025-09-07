package com.cafe.quiz.support.web.dto

import org.springframework.http.HttpStatus

object ApiStatusCode {
    enum class Success(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        OK("OK", "성공"),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.OK
    }

    enum class Unauthorized(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        UNAUTHORIZED("UNAUTHORIZED", "회원의 정보가 없어요. 먼저 로그인해주세요."),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.UNAUTHORIZED
    }

    enum class BadRequest(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        BAD_REQUEST("BAD_REQUEST", "요청 정보가 유효하지 않아요."),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.BAD_REQUEST
    }

    enum class ServerError(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "잠시 오류가 발생했어요. 잠시 후 다시 시도해주세요."),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.INTERNAL_SERVER_ERROR
    }
}
