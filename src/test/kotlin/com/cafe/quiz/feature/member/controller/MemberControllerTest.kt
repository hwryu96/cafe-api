package com.cafe.quiz.feature.member.controller

import com.cafe.quiz.feature.member.dto.MemberRegisterResponse
import com.cafe.quiz.feature.member.service.MemberService
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [MemberController::class])
class MemberControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var memberService: MemberService

    @Test
    fun `회원가입에_성공하면_200을_반환한다`() {
        // given
        whenever(memberService.register(any()))
            .thenReturn(MemberRegisterResponse(100L))

        val body =
            """
            {
              "name": "테스트",
              "phone": "010-1234-5678",
              "birth": "1996-08-29",
              "gender": "MALE"
            }
            """.trimIndent()

        // when & then
        mockMvc
            .post("/api/members") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.id", equalTo(100))
                jsonPath("$.code") { exists() }
            }
    }

    @Test
    fun `이름이_빈값이면_400을_반환한다`() {
        // given
        val body =
            """
            {
              "name": "",
              "phone": "010-1234-5678",
              "birth": "1996-08-29",
              "gender": "MALE"
            }
            """.trimIndent()

        // when & then
        mockMvc
            .post("/api/members") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.message", equalTo("이름을 입력해주세요."))
            }
    }

    @Test
    fun `전화번호_패턴이_유효하지_않으면_400을_반환한다`() {
        // given
        val body =
            """
            {
              "name": "테스트",
              "phone": "01012345678",
              "birth": "1996-08-29",
              "gender": "MALE"
            }
            """.trimIndent()

        // when & then
        mockMvc
            .post("/api/members") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.message", equalTo("전화번호 패턴이 유효하지 않아요."))
            }
    }

    @Test
    fun `생년월일_패턴이_유효하지_않으면_400을_반환한다`() {
        // given
        val body =
            """
            {
              "name": "테스트",
              "phone": "010-1234-5678",
              "birth": "19960829",
              "gender": "MALE"
            }
            """.trimIndent()

        // when & then
        mockMvc
            .post("/api/members") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.message", equalTo("생년월일 패턴이 유효하지 않아요."))
            }
    }
}
