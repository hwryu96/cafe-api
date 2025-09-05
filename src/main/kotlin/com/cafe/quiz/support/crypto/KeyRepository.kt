package com.cafe.quiz.support.crypto

interface KeyRepository {
    fun exists(keyId: String): Boolean

    fun find(keyId: String): ByteArray?

    fun save(
        keyId: String,
        keyBytes: ByteArray,
    )

    fun findAll(): List<String>
}
