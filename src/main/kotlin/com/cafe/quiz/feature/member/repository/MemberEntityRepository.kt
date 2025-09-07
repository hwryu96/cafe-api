package com.cafe.quiz.feature.member.repository

import com.cafe.quiz.feature.member.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberEntityRepository : JpaRepository<MemberEntity, Long>
