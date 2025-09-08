package com.cafe.quiz.support.lock

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class InMemoryDistributedLockTest {
    private val lock: DistributedLock = InMemoryDistributedLock()

    @Test
    fun `동일_키는_중복_획득이_불가능하다`() {
        // given
        val key = "order:1"

        // when
        val first = lock.tryLock(key, Duration.ofSeconds(5))
        val second = lock.tryLock(key, Duration.ofSeconds(5))

        // then
        assertThat(first).isTrue()
        assertThat(second).isFalse()
        assertThat(lock.isLocked(key)).isTrue()
    }

    @Test
    fun `잠금_해제시_해제된다`() {
        // given
        val key = "job:1"
        lock.tryLock(key, Duration.ofSeconds(5))

        // when
        val unlocked = lock.unlock(key)

        // then
        assertThat(unlocked).isTrue()
        assertThat(lock.isLocked(key)).isFalse()
    }

    @Test
    fun `존재하지_않는_키를_해제하면_거짓을_반환한다`() {
        // given
        val key = "nope"

        // when
        val unlocked = lock.unlock(key)

        // then
        assertThat(unlocked).isFalse()
    }

    @Test
    fun `시간이_만료되면_잠금이_해제된다`() {
        // given
        val key = "lease:1"
        val got = lock.tryLock(key, Duration.ofMillis(80))
        Thread.sleep(120) // 만료 대기

        // when
        val gotAfterExpire = lock.tryLock(key, Duration.ofSeconds(1))

        // then
        assertThat(got).isTrue()
        assertThat(gotAfterExpire).isTrue()
        assertThat(lock.isLocked(key)).isTrue()

        // 정리
        lock.unlock(key)
        assertThat(lock.isLocked(key)).isFalse()
    }

    @Test
    fun `withLockOrNull은_락을_획득하면_블록을_실행하고_반환값을_넘긴다`() {
        // given
        val key = "with:ok"

        // when
        val result = lock.withLockOrNull(key, Duration.ofSeconds(1)) { "돌았어요" }

        // then
        assertThat(result).isEqualTo("돌았어요")
        // 블록 종료 후 자동 unlock
        assertThat(lock.isLocked(key)).isFalse()
    }

    @Test
    fun `withLockOrNull은_락을_획득하지_못하면_널을_반환하고_블록을_실행하지_않는다`() {
        // given
        val key = "with:fail"
        val got = lock.tryLock(key, Duration.ofSeconds(3))
        var executed = false

        // when
        val result =
            lock.withLockOrNull(key, Duration.ofSeconds(1)) {
                executed = true
                "안돌아요"
            }

        // then
        assertThat(got).isTrue()
        assertThat(result).isNull()
        assertThat(executed).isFalse()

        // 정리
        lock.unlock(key)
        assertThat(lock.isLocked(key)).isFalse()
    }

    @Test
    fun `동시에_두_요청이_같은_키를_시도하면_하나만_성공한다`() {
        // given
        val key = "concurrent:1"
        val lease = Duration.ofSeconds(3)
        val pool = Executors.newFixedThreadPool(4)
        val latch = CountDownLatch(1)
        val success = AtomicInteger(0)

        // when
        repeat(4) {
            pool.submit {
                latch.await()
                if (lock.tryLock(key, lease)) {
                    success.incrementAndGet()
                }
            }
        }
        latch.countDown()
        pool.shutdown()
        pool.awaitTermination(2, TimeUnit.SECONDS)

        // then
        assertThat(success.get()).isEqualTo(1)

        // 정리 (잠금이 잡혔다면 해제)
        lock.unlock(key)
    }
}
