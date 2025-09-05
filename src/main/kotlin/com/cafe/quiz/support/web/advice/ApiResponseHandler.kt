package com.cafe.quiz.support.web.advice

import com.cafe.quiz.support.web.annotation.ApiController
import com.cafe.quiz.support.web.dto.ApiResponse
import com.cafe.quiz.support.web.dto.ApiStatusCode
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice(annotations = [ApiController::class])
class ApiResponseHandler : ResponseBodyAdvice<Any> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>?>,
    ) = AbstractJackson2HttpMessageConverter::class.java.isAssignableFrom(converterType)

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>?>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? =
        when (body) {
            is ApiResponse<*> -> body
            else ->
                ApiResponse(
                    code = ApiStatusCode.Success.OK.code,
                    message = ApiStatusCode.Success.OK.message,
                    data = body,
                )
        }
}
