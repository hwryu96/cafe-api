package com.cafe.quiz.feature.member.service

import com.cafe.quiz.feature.member.archive.ArchiveContext
import com.cafe.quiz.feature.member.archive.ArchiveProcessor
import com.cafe.quiz.feature.member.archive.MemberArchiveWorker
import com.cafe.quiz.feature.member.dto.MemberRegisterRequest
import com.cafe.quiz.feature.member.dto.MemberRegisterResponse
import com.cafe.quiz.feature.member.entity.MemberEntity
import com.cafe.quiz.feature.member.repository.ArchiveMemberEntityRepository
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import com.cafe.quiz.support.web.dto.ApiStatusCode
import com.cafe.quiz.support.web.exception.ApiException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class MemberService(
    private val memberEntityRepository: MemberEntityRepository,
    private val archiveMemberEntityRepository: ArchiveMemberEntityRepository,
    private val memberArchiveWorker: MemberArchiveWorker,
    private val archiveProcessors: List<ArchiveProcessor>,
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
            memberEntityRepository.findActiveMember(id).getOrNull()
                ?: throw ApiException(ApiStatusCode.NotFound.MEMBER_NOT_FOUND)

        // 데이터 삭제 플래그를 변경하여, 사용자에게 빠르게 응답 처리
        member.deleted = true

        // 삭제 이벤트 발행, 해당 지점에서 데이터를 분리보관 및 삭제 처리 한다.
        memberArchiveWorker.publish(id)
    }

    @Transactional
    fun restore(id: Long) {
        val archived =
            archiveMemberEntityRepository.findById(id).getOrNull()
                ?: throw ApiException(ApiStatusCode.NotFound.RESTORE_MEMBER_NOT_FOUND)

        if (!archived.canRestore()) {
            throw ApiException(ApiStatusCode.Conflict.RESTORE_WINDOW_EXPIRED)
        }

        for (archiveProcessor in archiveProcessors) {
            archiveProcessor.recover(
                ArchiveContext(
                    memberId = archived.id,
                ),
            )
        }
    }
}
