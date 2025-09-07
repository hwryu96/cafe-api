package com.cafe.quiz.feature.member.repository

import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.support.crypto.CryptoService
import com.cafe.quiz.support.crypto.InMemoryKeyRepository
import com.cafe.quiz.support.crypto.KeyManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@DataJpaTest
@Import(
    CryptoService::class,
    KeyManager::class,
    InMemoryKeyRepository::class,
)
class MemberEntityRepositoryTest {
    @Autowired
    lateinit var repository: MemberEntityRepository

    @Autowired
    lateinit var dataSource: DataSource

    @Autowired
    lateinit var cryptoService: CryptoService

    @Test
    fun `저장_후_조회가_가능하고_평문은_컨버터로_복호화된다`() {
        // given
        val entity =
            MemberEntity(
                name = "테스트",
                phone = "01012345678",
                birth = "1996-08-29",
                gender = GenderType.MALE,
            )

        // when
        val saved = repository.saveAndFlush(entity)
        val found = repository.findById(saved.id).orElseThrow()

        // then
        assertThat(found.name).isEqualTo("테스트")
        assertThat(found.phone).isEqualTo("01012345678")
        assertThat(found.birth).isEqualTo("1996-08-29")
    }

    @Test
    fun `실제_컬럼에는_암호문이_저장된다`() {
        // given
        val e =
            repository.saveAndFlush(
                MemberEntity(
                    name = "테스트",
                    phone = "01012345678",
                    birth = "1996-08-29",
                    gender = GenderType.FEMALE,
                ),
            )

        // when
        val jdbc = JdbcTemplate(dataSource)
        val row =
            jdbc.queryForMap(
                "SELECT name_enc, phone_enc, birth_enc FROM member WHERE id = ?",
                e.id,
            )

        // then
        val token = Regex("""^v1\.[0-9a-fA-F]{32}\.[A-Za-z0-9+/=]+\.[A-Za-z0-9+/=]+$""").toPattern()
        assertThat(row["NAME_ENC"] as String).matches(token)
        assertThat(row["PHONE_ENC"] as String).matches(token)
        assertThat(row["BIRTH_ENC"] as String).matches(token)
    }

    @Test
    fun `검색용_해시가_저장된다`() {
        // given
        val name = "테스트"
        val phone = "01012345678"
        val birth = "1996-08-29"

        val entity =
            MemberEntity(
                name = name,
                phone = phone,
                birth = birth,
                gender = GenderType.MALE,
            )

        // when
        val saved = repository.saveAndFlush(entity)

        // then
        val jdbc = JdbcTemplate(dataSource)
        val row =
            jdbc.queryForMap(
                "SELECT name_hash, phone_hash FROM member WHERE id = ?",
                saved.id,
            )

        val nameHash = cryptoService.hash(name)
        val phoneHash = cryptoService.hash(phone)

        assertThat(row["NAME_HASH"] as String)
            .hasSize(64)
            .matches(Regex("^[0-9a-f]{64}$").toPattern())
            .isEqualTo(nameHash)

        assertThat(row["PHONE_HASH"] as String)
            .hasSize(64)
            .matches(Regex("^[0-9a-f]{64}$").toPattern())
            .isEqualTo(phoneHash)
    }
}
