package com.cafe.quiz.feature.member.service

import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {
    @Mock
    private lateinit var repo: MemberEntityRepository

    @InjectMocks
    private lateinit var service: MemberService

    @Captor
    lateinit var memberEntityCaptor: ArgumentCaptor<MemberEntity>

    @Test
    fun `정상적인_흐름에서의_회원가입은_성공한다`() {
        // given
        val req =
            MemberRegisterRequest(
                name = "테스트",
                phone = "010-1234-5678",
                birth = "1996-08-29",
                gender = GenderType.MALE,
            )

        doAnswer { it.arguments[0] }
            .whenever(repo)
            .save(any())

        // when
        service.register(req)

        // then
        verify(repo).save(capture(memberEntityCaptor))
        val saved = memberEntityCaptor.value

        assertThat(saved.name).isEqualTo(req.name)
        assertThat(saved.phone).isEqualTo(req.phone)
        assertThat(saved.birth).isEqualTo(req.birth)
        assertThat(saved.gender).isEqualTo(req.gender)
    }
}
