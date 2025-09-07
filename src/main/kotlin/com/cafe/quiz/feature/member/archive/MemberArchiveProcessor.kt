package com.cafe.quiz.feature.member.archive

import com.cafe.quiz.feature.member.entity.ArchiveMemberEntity
import com.cafe.quiz.feature.member.repository.ArchiveMemberEntityRepository
import com.cafe.quiz.feature.member.repository.MemberEntityRepository
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

/**
 * 회원의 정보를 아카이브 하기 위한 프로세서
 */
@Component
class MemberArchiveProcessor(
    private val memberEntityRepository: MemberEntityRepository,
    private val archiveMemberEntityRepository: ArchiveMemberEntityRepository,
) : ArchiveProcessor {
    companion object {
        const val MEMBER_ID_FIELD = "MEMBER_ID"
    }

    override fun process(memberId: Long): ArchiveContext {
        val member =
            memberEntityRepository.findById(memberId).getOrNull()
                ?: throw IllegalStateException("Member entity $memberId 가 없음. 이미 아카이브 되었는지 확인 필요함")

        archiveMemberEntityRepository.save(ArchiveMemberEntity.from(member))
        memberEntityRepository.delete(member)

        return ArchiveContext().apply {
            set(MEMBER_ID_FIELD, memberId)
        }
    }

    override fun recover(context: ArchiveContext): ArchiveContext {
        val memberId = context.get<Long>(MEMBER_ID_FIELD)!!
        val archived =
            archiveMemberEntityRepository.findById(memberId).getOrNull()
                ?: throw IllegalStateException("복구 로직 실행 중 문제 발생. 아카이브 된 회원정보 없음. id=$memberId")

        memberEntityRepository.save(archived.toMember())
        archiveMemberEntityRepository.delete(archived)
        return context
    }
}
