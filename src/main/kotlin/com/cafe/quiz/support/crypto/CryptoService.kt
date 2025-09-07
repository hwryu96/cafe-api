package com.cafe.quiz.support.crypto

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

@Component
class CryptoService(
    private val keyManager: KeyManager,
) {
    private val version = "v1"
    private val random = SecureRandom()
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()
    private val algorithm = "AES/GCM/NoPadding"

    fun encrypt(plain: String?): String? {
        if (plain.isNullOrBlank()) {
            return plain
        }

        val keyId = keyManager.currentKeyId()
        val key = keyManager.currentKey()
        val iv = ByteArray(12).also { random.nextBytes(it) }

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))

        return buildString {
            append(version)
            append(".")
            append(keyId)
            append('.')
            append(encoder.encodeToString(iv))
            append('.')
            append(encoder.encodeToString(ct))
        }
    }

    fun decrypt(token: String?): String? {
        if (token.isNullOrBlank()) {
            return token
        }

        val parts = token.split('.')
        require(parts.size == 4 && parts[0] == version) {
            "Invalid token format"
        }

        val keyId = parts[1]
        val iv = decoder.decode(parts[2])
        val ct = decoder.decode(parts[3])
        val key = keyManager.getKey(keyId)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val pt = cipher.doFinal(ct)
        return String(pt, Charsets.UTF_8)
    }

    fun hash(input: String?): String? {
        if (input.isNullOrBlank()) {
            return input
        }

        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
