package com.cafe.quiz.support.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.datasource")
class CustomDataSourceProperties : LinkedHashMap<String, CustomDataSourceProperties.Properties>() {
    data class Properties(
        var url: String = "",
        var username: String = "",
        var password: String = "",
        var driverClassName: String = "org.h2.Driver",
    )

    fun getPropertyOrThrow(name: String) =
        this[name]
            ?: throw IllegalArgumentException("DataSource $name 이 존재하지 않습니다.")
}
