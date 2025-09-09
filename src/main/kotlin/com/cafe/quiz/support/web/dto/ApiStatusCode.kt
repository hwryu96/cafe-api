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

    enum class NotFound(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원 정보가 존재하지 않아요."),
        RESTORE_MEMBER_NOT_FOUND("RESTORE_MEMBER_NOT_FOUND", "탈퇴를 철회할 회원의 정보가 존재하지 않아요."),
        ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문 내역이 존재하지 않아요."),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.NOT_FOUND
    }

    enum class Conflict(
        override val code: String,
        override val message: String,
    ) : StatusCode {
        RESTORE_WINDOW_EXPIRED("RESTORE_WINDOW_EXPIRED", "탈퇴 철회 가능 기간이 지났어요."),
        REQUEST_IN_PROGRESS("REQUEST_IN_PROGRESS", "이미 처리중이에요, 잠시만 기다려주세요."),
        CANCEL_NOT_ALLOWED("CANCEL_NOT_ALLOWED", "해당 주문은 취소할 수 없어요."),
        LIMIT_EXCEEDED("LIMIT_EXCEEDED", "한도가 부족해요. 한도를 확인 후 다시 주문해주세요."),
        REFUNDABLE_PAYMENT_NOT_FOUND("REFUNDABLE_PAYMENT_NOT_FOUND", "환불 가능한 결제 내역이 존재하지 않아요."),
        ORDER_FAILED("ORDER_FAILED", "결제가 실패했어요. 잠시 후 다시 시도해주세요."),
        ;

        override val httpStatus: HttpStatus
            get() = HttpStatus.CONFLICT
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
