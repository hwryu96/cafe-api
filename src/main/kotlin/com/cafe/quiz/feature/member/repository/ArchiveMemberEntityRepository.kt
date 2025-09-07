package com.cafe.quiz.feature.member.repository

import com.cafe.quiz.feature.member.entity.ArchiveMemberEntity
import com.cafe.quiz.support.jpa.annotation.ArchiveRepository
import org.springframework.data.jpa.repository.JpaRepository

@ArchiveRepository
interface ArchiveMemberEntityRepository : JpaRepository<ArchiveMemberEntity, Long>
