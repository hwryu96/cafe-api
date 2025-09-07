package com.cafe.quiz.support.web.argumentresolver

import com.cafe.quiz.support.const.HeaderConstant
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.dto.MemberContext
import com.cafe.quiz.support.web.exception.ApiException
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 로그인 회원 정보 context를 생성해 주는 argument resolver.
 * MemberContext가 컨트롤러 파라미터에 있으나, X-CAFE-MEMBER-ID 헤더가 없을 경우 401 예외가 발생한다.
 */
class MemberContextArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = MemberContext::class.java.isAssignableFrom(parameter.parameterType)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val memberId =
            webRequest.getHeader(HeaderConstant.X_CAFE_MEMBER_ID)?.toLong()
                ?: throw ApiException(ApiStatusCode.Unauthorized.UNAUTHORIZED)

        return MemberContext(memberId)
    }
}
