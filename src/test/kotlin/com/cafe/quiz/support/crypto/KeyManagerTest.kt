package com.cafe.quiz.support.crypto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class KeyManagerTest {
    private class TestKeyRepository : KeyRepository {
        private val map = linkedMapOf<String, ByteArray>()

        override fun exists(keyId: String) = map.containsKey(keyId)

        override fun find(keyId: String) = map[keyId]

        override fun save(
            keyId: String,
            keyBytes: ByteArray,
        ) {
            map[keyId] = keyBytes
        }

        override fun findAll(): List<String> = map.keys.toList()
    }

    @Test
    fun `초기화_시_활성키가_생성된다`() {
        // when
        val km = KeyManager(TestKeyRepository())

        // then
        val activeId = km.currentKeyId()
        val activeKey = km.currentKey()

        assertThat(activeId).isNotBlank()
        assertThat(activeKey.encoded.size).isGreaterThanOrEqualTo(32)
    }

    @Test
    fun `키가_없으면_예외가_발생한다`() {
        // given
        val km = KeyManager(TestKeyRepository())
        val keyId = "nope"

        // when & then
        assertThatThrownBy { km.getKey(keyId) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `새_키_생성_시_활성키가_전환되고_이전키는_유지된다`() {
        // given
        val repo = TestKeyRepository()
        val km = KeyManager(repo)
        val firstId = km.currentKeyId()
        val firstKey = km.getKey(firstId)

        // when
        val secondId = km.generateAndActivateNew()
        val secondKey = km.getKey(secondId)

        // then
        assertThat(secondId).isNotEqualTo(firstId)

        assertThat(km.getKey(firstId)).isNotNull()
        assertThat(km.getKey(secondId)).isNotNull()
        assertThat(firstKey.encoded.size).isGreaterThanOrEqualTo(32)
        assertThat(secondKey.encoded.size).isGreaterThanOrEqualTo(32)
    }
}
