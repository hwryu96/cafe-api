package com.cafe.quiz.feature.member.entity

import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.support.const.CafeConstant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ArchiveMemberEntityTest {
    private fun entity(
        archivedAt: LocalDateTime,
        destroyed: Boolean = false,
    ) = ArchiveMemberEntity(
        id = 1L,
        name = "테스트",
        phone = "010-1234-5678",
        birth = "1996-08-29",
        gender = GenderType.MALE,
        createdAt = LocalDateTime.now().minusDays(40),
        updatedAt = LocalDateTime.now(),
        archivedAt = archivedAt,
        destroyed = destroyed,
    )

    @Test
    fun `철회_기간이_지나지_않았으면_복구가_가능하다`() {
        // given
        val now = LocalDateTime.now()
        val withinWindow =
            now
                .minusDays(CafeConstant.MEMBER_RESTORE_DAYS)
                .plusSeconds(5)

        val archived = entity(withinWindow)

        // when
        val result = archived.canRestore()

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `철회_기간이_종료되면_복구가_불가하다`() {
        // given
        val now = LocalDateTime.now()
        val passedWindow =
            now
                .minusDays(CafeConstant.MEMBER_RESTORE_DAYS)
                .minusSeconds(5)

        val archived = entity(passedWindow)

        // when
        val result = archived.canRestore()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `데이터_파기가_완료되면_복구가_불가하다`() {
        // given
        val withinWindow = LocalDateTime.now().minusDays(1)
        val archived = entity(withinWindow, destroyed = true)

        // when
        val result = archived.canRestore()

        // then
        assertThat(result).isFalse()
    }
}
