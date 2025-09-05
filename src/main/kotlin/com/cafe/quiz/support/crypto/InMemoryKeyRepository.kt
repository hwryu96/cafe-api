package com.cafe.quiz.support.crypto

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryKeyRepository : KeyRepository {
    private val map = ConcurrentHashMap<String, ByteArray>()

    override fun exists(keyId: String) = map.containsKey(keyId)

    override fun find(keyId: String) = map[keyId]

    override fun save(
        keyId: String,
        keyBytes: ByteArray,
    ) {
        map.putIfAbsent(keyId, keyBytes)?.let {
            throw IllegalStateException("keyId already exists: $keyId")
        }
    }

    override fun findAll(): List<String> = map.keys().toList()
}
