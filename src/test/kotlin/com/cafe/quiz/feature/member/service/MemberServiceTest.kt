package com.cafe.quiz.feature.member.service

import com.cafe.quiz.feature.member.archive.MemberArchiveWorker
import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import com.cafe.quiz.support.web.exception.ApiException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {
    @Mock
    private lateinit var repo: MemberEntityRepository

    @Mock
    private lateinit var worker: MemberArchiveWorker

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

    @Test
    fun `탈퇴시_회원이_존재하면_삭제_플래그가_설정되고_아카이브_워커를_호출한다`() {
        // given
        val entity =
            MemberEntity(
                id = 100L,
                name = "테스트",
                phone = "010-1234-5678",
                birth = "1996-08-29",
                gender = GenderType.MALE,
            )

        whenever(repo.findById(100L)).thenReturn(Optional.of(entity))
        whenever(worker.publish(100L)).thenReturn(true)

        // when
        service.withdraw(100L)

        // then
        assertThat(entity.deleted).isTrue()
        verify(worker).publish(100L)
    }

    @Test
    fun `탈퇴시_회원이_존재하지_않으면_예외가_발생한다`() {
        // given
        whenever(repo.findById(99L)).thenReturn(Optional.empty())

        // when & then
        assertThatThrownBy { service.withdraw(99L) }
            .isInstanceOf(ApiException::class.java)

        verify(worker, never()).publish(any())
    }
}
