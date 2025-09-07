package com.cafe.quiz.feature.member.service

import com.cafe.quiz.feature.member.archive.MemberArchiveWorker
import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.dto.MemberRegisterResponse
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.exception.ApiException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class MemberService(
    private val memberEntityRepository: MemberEntityRepository,
    private val memberArchiveWorker: MemberArchiveWorker,
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

    @Transactional
    fun withdraw(id: Long) {
        val member =
            memberEntityRepository.findById(id).getOrNull()
                ?: throw ApiException(ApiStatusCode.NotFound.MEMBER_NOT_FOUND)

        // 데이터 삭제 플래그를 변경하여, 사용자에게 빠르게 응답 처리
        member.deleted = true

        // 삭제 이벤트 발행, 해당 지점에서 데이터를 분리보관 및 삭제 처리 한다.
        memberArchiveWorker.publish(id)
    }
}
