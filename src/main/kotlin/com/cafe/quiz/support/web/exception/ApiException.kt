package com.cafe.quiz.support.web.exception

import com.cafe.quiz.support.web.dto.StatusCode

class ApiException(
    val code: StatusCode,
) : RuntimeException()
