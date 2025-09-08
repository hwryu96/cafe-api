package com.cafe.quiz.support.lock

import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryDistributedLock : DistributedLock {
    private data class LockRecord(
        val expiryEpochMillis: Long,
    ) {
        fun isExpired(now: Long = System.currentTimeMillis()) = expiryEpochMillis <= now
    }

    private val locks = ConcurrentHashMap<String, LockRecord>()

    override fun tryLock(
        key: String,
        leaseTime: Duration,
    ): Boolean {
        val now = System.currentTimeMillis()
        val newRec = LockRecord(now + leaseTime.toMillis())

        val result =
            locks.compute(key) { _, current ->
                if (current == null || current.isExpired(now)) newRec else current
            }

        return result === newRec
    }

    override fun unlock(key: String): Boolean = locks.remove(key) != null

    override fun isLocked(key: String): Boolean {
        val rec = locks[key] ?: return false
        if (rec.isExpired()) {
            locks.remove(key, rec)
            return false
        }

        return true
    }
}
