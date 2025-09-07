package com.cafe.quiz.support.generator

import java.util.UUID

object IdGenerator {
    fun generate(): Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
}
