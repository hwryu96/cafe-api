package com.cafe.quiz.feature.member.archive

import com.cafe.quiz.feature.member.entity.ArchiveMemberEntity
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.feature.member.repository.ArchiveMemberEntityRepository
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
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberArchiveProcessorTest {
    @Mock
    private lateinit var memberRepo: MemberEntityRepository

    @Mock
    private lateinit var archiveRepo: ArchiveMemberEntityRepository

    @InjectMocks
    private lateinit var processor: MemberArchiveProcessor

    @Captor
    private lateinit var memberEntityCaptor: ArgumentCaptor<MemberEntity>

    @Captor
    private lateinit var archiveMemberEntityCaptor: ArgumentCaptor<ArchiveMemberEntity>

    @Test
    fun `아카이브에_회원정보를_저장하고_원본을_삭제한다`() {
        // given
        val m =
            MemberEntity(
                id = 10L,
                name = "테스트",
                phone = "010-1234-5678",
                birth = "1996-08-29",
                gender = GenderType.MALE,
                deleted = false,
                createdAt = LocalDateTime.now().minusDays(1),
                updatedAt = LocalDateTime.now(),
            )

        whenever(memberRepo.findById(10L)).thenReturn(Optional.of(m))

        doAnswer { it.arguments[0] }
            .whenever(archiveRepo)
            .save(any())

        // when
        processor.process(10L)

        // then
        verify(archiveRepo).save(capture(archiveMemberEntityCaptor))
        val saved = archiveMemberEntityCaptor.value

        assertThat(saved.id).isEqualTo(10L)
        assertThat(saved.name).isEqualTo(m.name)
        assertThat(saved.phone).isEqualTo(m.phone)
        assertThat(saved.birth).isEqualTo(m.birth)
        assertThat(saved.gender).isEqualTo(m.gender)

        verify(memberRepo).delete(m)
    }

    @Test
    fun `아카이브의_회원정보를_복구한다`() {
        // given
        val archived =
            ArchiveMemberEntity(
                id = 20L,
                name = "테스트",
                phone = "010-1234-5678",
                birth = "1996-08-29",
                gender = GenderType.MALE,
                createdAt = LocalDateTime.now().minusDays(1),
                updatedAt = LocalDateTime.now(),
            )
        whenever(archiveRepo.findById(20L)).thenReturn(Optional.of(archived))
        doAnswer { it.arguments[0] }
            .whenever(memberRepo)
            .save(any())

        val context = ArchiveContext().apply { set(MemberArchiveProcessor.MEMBER_ID_FIELD, 20L) }

        // when
        processor.recover(context)

        // then
        verify(memberRepo).save(capture(memberEntityCaptor))
        val saved = memberEntityCaptor.value

        assertThat(saved.id).isEqualTo(20L)
        assertThat(saved.name).isEqualTo(archived.name)
        assertThat(saved.phone).isEqualTo(archived.phone)
        assertThat(saved.birth).isEqualTo(archived.birth)
        assertThat(saved.gender).isEqualTo(archived.gender)

        verify(archiveRepo).delete(archived)
    }
}
