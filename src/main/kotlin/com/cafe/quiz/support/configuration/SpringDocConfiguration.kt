package com.cafe.quiz.support.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfiguration {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .addServersItem(Server().url("/"))
            .components(Components())
            .info(
                Info()
                    .title("Cafe API")
                    .description("Cafe API")
                    .version("v1"),
            )
}
