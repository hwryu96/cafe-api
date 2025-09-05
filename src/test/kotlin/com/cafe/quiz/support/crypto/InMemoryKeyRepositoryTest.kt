package com.cafe.quiz.support.crypto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InMemoryKeyRepositoryTest {
    @Test
    fun `새_키를_저장한다`() {
        // given
        val repo = InMemoryKeyRepository()
        val keyId = "k1"
        val keyBytes = ByteArray(32) { 0x11 }

        // when
        repo.save(keyId, keyBytes)

        // then
        assertThat(repo.exists(keyId)).isTrue()
        assertThat(repo.find(keyId)).isEqualTo(keyBytes)
        assertThat(repo.findAll()).contains(keyId)
    }

    @Test
    fun `동일_키로_두번_저장하면_예외가_발생한다`() {
        // given
        val repo = InMemoryKeyRepository()
        val keyId = "dup"

        val first = ByteArray(32) { 0x22 }
        val second = ByteArray(32) { 0x33 }

        repo.save(keyId, first)

        // when / then
        assertThatThrownBy { repo.save(keyId, second) }
            .isInstanceOf(IllegalStateException::class.java)

        assertThat(repo.find(keyId)).isEqualTo(first)
    }

    @Test
    fun `존재하지_않는_키는_데이터를_반환하지_않는다`() {
        // given
        val repo = InMemoryKeyRepository()

        // when
        val exists = repo.exists("nope")
        val found = repo.find("nope")

        // then
        assertThat(exists).isFalse()
        assertThat(found).isNull()
    }
}
