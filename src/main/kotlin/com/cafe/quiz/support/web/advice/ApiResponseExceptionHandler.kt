package com.cafe.quiz.support.web.advice

import com.cafe.quiz.support.web.dto.ApiResponse
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.exception.ApiException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val log = KotlinLogging.logger {}

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiResponseExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<Any> =
        ResponseEntity(
            ApiResponse.from(ex.code),
            ex.code.httpStatus,
        )

    @ExceptionHandler(value = [IllegalArgumentException::class, ConstraintViolationException::class])
    fun handleIllegalArgumentException(ex: Exception): ResponseEntity<Any> {
        val code = ApiStatusCode.BadRequest.BAD_REQUEST

        return ResponseEntity(
            ApiResponse.from(code),
            code.httpStatus,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUncaughtException(ex: Exception): ResponseEntity<Any> {
        log.error(ex) { "오류 발생!" }

        val code = ApiStatusCode.ServerError.INTERNAL_SERVER_ERROR

        return ResponseEntity(
            ApiResponse.from(code),
            code.httpStatus,
        )
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val code = ApiStatusCode.BadRequest.BAD_REQUEST

        return ResponseEntity(
            ApiResponse.from(code),
            code.httpStatus,
        )
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val first = ex.bindingResult.allErrors.first()
        val code = ApiStatusCode.BadRequest.BAD_REQUEST

        return ResponseEntity(
            ApiResponse<Any>(
                code.code,
                first.defaultMessage ?: code.message,
            ),
            code.httpStatus,
        )
    }
}
