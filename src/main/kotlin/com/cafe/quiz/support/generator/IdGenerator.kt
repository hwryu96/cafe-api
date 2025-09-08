package com.cafe.quiz.support.generator

/**
 * 테스트 편의성을 위해 간단한 시간 기반의 아이디 제네레이터를 생성
 */
object IdGenerator {
    fun generate(): Long = System.currentTimeMillis() shl 12
}
