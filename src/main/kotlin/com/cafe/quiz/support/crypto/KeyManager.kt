package com.cafe.quiz.support.crypto

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

private val log = KotlinLogging.logger {}

@Component
class KeyManager(
    private val repo: KeyRepository,
) {
    private val algorithm = "AES"
    private val random = SecureRandom()
    private val activeIdRef = AtomicReference<String>()

    init {
        generateAndActivateNew()
    }

    fun currentKeyId(): String = activeIdRef.get()

    fun currentKey(): SecretKey = keyFor(currentKeyId())

    fun getKey(keyId: String): SecretKey = keyFor(keyId)

    @Synchronized
    fun generateAndActivateNew(): String {
        val keyId = generateNewKeyId()
        val bytes =
            ByteArray(32)
                .also { random.nextBytes(it) }

        if (!repo.exists(keyId)) {
            repo.save(keyId, bytes)
        }

        activeIdRef.set(keyId)
        return keyId
    }

    private fun generateNewKeyId(): String = UUID.randomUUID().toString().replace("-", "")

    private fun keyFor(keyId: String): SecretKeySpec {
        val raw =
            repo.find(keyId)
                ?: throw IllegalStateException("Key not found: $keyId")

        return SecretKeySpec(raw.copyOf(32), algorithm)
    }

    /**
     * 실제로는 키 관리 서비스가 따로 존재하겠으나,
     * 이 환경에서는 키가 로테이트하는 행위를 간략하게 보기 위한 스케줄링.
     */
    @Scheduled(cron = "0 * * * * *")
    fun rotate() {
        generateAndActivateNew()
        log.info { "Rotating key: keyId=${currentKeyId()}" }
    }
}
