package com.cafe.quiz.feature.member.service

import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.dto.MemberRegisterResponse
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberEntityRepository: MemberEntityRepository,
) {
    fun register(request: MemberRegisterRequest): MemberRegisterResponse {
        val saved =
            memberEntityRepository.save(
                MemberEntity(
                    name = request.name,
                    birth = request.birth,
                    phone = request.phone,
                    gender = request.gender,
                ),
            )

        return MemberRegisterResponse(saved.id)
    }
}
