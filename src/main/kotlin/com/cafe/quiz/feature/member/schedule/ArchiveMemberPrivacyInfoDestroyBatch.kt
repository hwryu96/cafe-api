package com.cafe.quiz.feature.member.schedule

import com.cafe.quiz.feature.member.repository.ArchiveMemberEntityRepository
import com.cafe.quiz.support.const.CafeConstant
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 회원의 개인정보를 파기하는 배치
 */
private val log = KotlinLogging.logger {}

@Component
class ArchiveMemberPrivacyInfoDestroyBatch(
    private val archiveMemberEntityRepository: ArchiveMemberEntityRepository,
) {
    /**
     * 1분에 한번씩 탈퇴 철회가 불가능한 회원의 정보를 파기한다
     * (개인정보 파기를 보여주는 시나리오)
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    fun privacyInfoDestroyBatch() {
        log.info { "개인정보 파기 배치 시작!" }

        val threshold =
            LocalDateTime
                .now()
                .minusDays(CafeConstant.MEMBER_RESTORE_DAYS)

        val targets = archiveMemberEntityRepository.findPrivacyInfoDestroyTargets(threshold)
        targets.forEach { it.destroy() }

        log.info { "개인정보 파기 배치 종료! size=${targets.size}" }
    }
}
