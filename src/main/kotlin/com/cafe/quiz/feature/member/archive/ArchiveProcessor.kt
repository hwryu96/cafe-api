package com.cafe.quiz.feature.member.archive

/**
 * 데이터를 아카이브 하기 위한 프로세서.
 * 해당 프로세서를 구현하면, 회원 탈퇴 시 해당 프로세서를 호출해줍니다.
 */
interface ArchiveProcessor {
    /**
     * 데이터 아카이브를 위한 구현
     * 해당 메서드에서 데이터를 아카이브 처리하고 추후 복구를 위한 컨텍스트를 반환하세요.
     */
    fun process(memberId: Long): ArchiveContext

    /**
     * 데이터 아카이브를 복구하기 위한 구현
     * 해당 메서드에서 아카이브한 데이터를 복구하세요
     */
    fun recover(context: ArchiveContext): ArchiveContext = context
}
