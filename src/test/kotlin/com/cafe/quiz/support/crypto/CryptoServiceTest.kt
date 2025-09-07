package com.cafe.quiz.support.crypto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CryptoServiceTest {
    private lateinit var repo: TestKeyRepository
    private lateinit var keyManager: KeyManager
    private lateinit var crypto: CryptoService

    @BeforeEach
    fun setUp() {
        repo = TestKeyRepository()
        keyManager = KeyManager(repo)
        crypto = CryptoService(keyManager)
    }

    @Test
    fun `평문을_암호화하면_토큰_포맷을_만족하고_복호화가_가능하다`() {
        // given
        val plain = "테스트 메시지"
        val tokenPattern = """^v1\.[0-9a-fA-F]{32}\.[A-Za-z0-9+/=]+\.[A-Za-z0-9+/=]+$"""

        // when
        val token = crypto.encrypt(plain)
        val decrypted = crypto.decrypt(token)

        // then
        assertThat(token).isNotNull()
        assertThat(token!!)
            .matches(Regex(tokenPattern).toPattern())
        assertThat(decrypted).isEqualTo(plain)
    }

    @Test
    fun `키가_회전해도_이전_토큰을_복호화할_수_있다`() {
        // given
        val beforeRotation = crypto.encrypt("first-data")!!
        val firstKeyId = keyManager.currentKeyId()

        // when
        val rotatedKeyId = keyManager.generateAndActivateNew()
        val afterRotation = crypto.encrypt("second-data")!!

        // then
        // 활성 keyId가 변경
        assertThat(rotatedKeyId).isNotEqualTo(firstKeyId)

        // 새 토큰은 새 keyId로 암호화됨
        assertThat(afterRotation.split('.')[1]).isEqualTo(rotatedKeyId)

        // 이전 토큰과 현재 토큰 모두 복호화 가능
        assertThat(crypto.decrypt(beforeRotation)).isEqualTo("first-data")
        assertThat(crypto.decrypt(afterRotation)).isEqualTo("second-data")
    }

    @Test
    fun `널과_빈문자열은_그대로_반환한다`() {
        // given
        val nullInput: String? = null
        val emptyInput = ""

        // when
        val encNull = crypto.encrypt(nullInput)
        val encEmpty = crypto.encrypt(emptyInput)
        val decNull = crypto.decrypt(nullInput)
        val decEmpty = crypto.decrypt(emptyInput)

        // then
        assertThat(encNull).isNull()
        assertThat(encEmpty).isEqualTo("")
        assertThat(decNull).isNull()
        assertThat(decEmpty).isEqualTo("")
    }

    @Test
    fun `토큰이_변조되면_복호화가_실패한다`() {
        // given
        val token = crypto.encrypt("data")!!

        // when
        val tampered = token.dropLast(1) + (token.last() + 1)

        // then
        assertThatThrownBy { crypto.decrypt(tampered) }
            .isInstanceOf(Exception::class.java)
    }

    @Test
    fun `토큰_포맷이_아니면_예외를_던진다`() {
        // given
        val invalid = "v0.bad.token"

        // when & then
        assertThatThrownBy { crypto.decrypt(invalid) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `같은_입력은_항상_동일_해시를_반환한다`() {
        // given
        val input = "테스트 메시지"

        // when
        val h1 = crypto.hash(input)
        val h2 = crypto.hash(input)

        // then
        assertThat(h1).isNotNull()
        assertThat(h1).isEqualTo(h2)
    }

    @Test
    fun `다른_입력은_서로_다른_해시를_반환한다`() {
        // given
        val input1 = "test-1"
        val input2 = "test-2"

        // when
        val hash1 = crypto.hash(input1)
        val hash2 = crypto.hash(input2)

        // then
        assertThat(hash1).isNotNull()
        assertThat(hash2).isNotNull()
        assertThat(hash1).isNotEqualTo(hash2)
    }
}
