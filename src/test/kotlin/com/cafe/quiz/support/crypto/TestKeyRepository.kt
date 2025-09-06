package com.cafe.quiz.support.crypto

class TestKeyRepository : KeyRepository {
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
