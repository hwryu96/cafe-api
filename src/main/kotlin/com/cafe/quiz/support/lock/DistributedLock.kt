package com.cafe.quiz.support.lock

import java.time.Duration

/**
 * 분산락 추상화.
 * 현재는 가볍게 인메모리로 구현했기 때문에, 다중 인스턴스 환경에서는 보장되지 않음.
 */
interface DistributedLock {
    fun tryLock(
        key: String,
        leaseTime: Duration = Duration.ofSeconds(30),
    ): Boolean

    fun unlock(key: String): Boolean

    fun isLocked(key: String): Boolean

    fun <T> withLockOrNull(
        key: String,
        leaseTime: Duration = Duration.ofSeconds(30),
        block: () -> T,
    ): T? {
        if (!tryLock(key, leaseTime)) return null
        try {
            return block()
        } finally {
            unlock(key)
        }
    }
}
