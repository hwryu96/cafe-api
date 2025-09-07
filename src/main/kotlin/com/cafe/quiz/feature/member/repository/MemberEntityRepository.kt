package com.cafe.quiz.feature.member.repository

import com.cafe.quiz.feature.member.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface MemberEntityRepository : JpaRepository<MemberEntity, Long> {
    @Query("select m from MemberEntity m where m.id=:id and m.deleted=false")
    fun findActiveMember(id: Long): Optional<MemberEntity>
}
