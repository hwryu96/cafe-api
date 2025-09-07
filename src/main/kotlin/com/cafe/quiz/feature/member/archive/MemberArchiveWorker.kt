package com.cafe.quiz.feature.member.archive

import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrNull

/**
 * 해당 워커에서 회원 탈퇴 분리 보관 및 데이터 삭제 처리한다.
 */
private val log = KotlinLogging.logger {}

@Component
class MemberArchiveWorker(
    private val memberEntityRepository: MemberEntityRepository,
    private val archiveProcessors: List<ArchiveProcessor>,
) {
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    private val channel = Channel<Long>(Channel.BUFFERED)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val activated = AtomicBoolean(false)

    init {
        startWorker()
    }

    fun publish(memberId: Long) = channel.trySend(memberId).isSuccess

    private fun startWorker() {
        if (activated.compareAndSet(false, true)) {
            repeat(2) {
                scope.launch {
                    consumeLoop()
                }
            }

            log.info { "회원 정보 분리보관 Worker 시작!" }
        }
    }

    private suspend fun consumeLoop() {
        for (memberId in channel) {
            process(memberId)
        }
    }

    private suspend fun process(memberId: Long) {
        val exists = memberEntityRepository.findById(memberId).getOrNull()
        if (exists == null) {
            log.warn { "탈퇴/분리보관 대상 회원이 존재하지 않음. id=$memberId" }
            return
        }

        val executed = mutableListOf<Pair<ArchiveProcessor, ArchiveContext>>()

        runCatching {
            for (p in archiveProcessors) {
                val ctx = p.process(memberId)
                executed += p to ctx
            }
        }.onSuccess {
            log.info { "회원 분리보관 파이프라인 완료. id=$memberId, steps=${archiveProcessors.size}" }
        }.onFailure { ex ->
            log.error(ex) { "회원 분리보관 실패! 복구 시작. id=$memberId" }
            executed.asReversed().forEach { (p, ctx) ->
                runCatching { p.recover(ctx) }
                    .onFailure { e -> log.error(e) { "복구 실패 id=$memberId" } }
            }
        }
    }
}
