package com.cafe.quiz.support.web.dto

import org.springframework.http.HttpStatus

interface StatusCode {
    val code: String
    val httpStatus: HttpStatus
    val message: String
}
