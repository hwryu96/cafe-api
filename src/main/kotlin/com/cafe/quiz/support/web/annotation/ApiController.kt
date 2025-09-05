package com.cafe.quiz.support.web.annotation

import org.springframework.web.bind.annotation.RestController

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@RestController
annotation class ApiController
