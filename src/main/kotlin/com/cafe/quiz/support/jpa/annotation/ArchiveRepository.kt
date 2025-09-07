package com.cafe.quiz.support.jpa.annotation

import org.springframework.stereotype.Repository

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repository
annotation class ArchiveRepository
