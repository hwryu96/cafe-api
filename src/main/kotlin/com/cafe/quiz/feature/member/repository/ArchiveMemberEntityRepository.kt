package com.cafe.quiz.feature.member.repository

import com.cafe.quiz.feature.member.entity.ArchiveMemberEntity
import com.cafe.quiz.support.jpa.annotation.ArchiveRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

@ArchiveRepository
interface ArchiveMemberEntityRepository : JpaRepository<ArchiveMemberEntity, Long> {
    @Query("select m from ArchiveMemberEntity m where m.destroyed = false and m.archivedAt <= :threshold order by m.archivedAt asc")
    fun findPrivacyInfoDestroyTargets(threshold: LocalDateTime): List<ArchiveMemberEntity>
}
