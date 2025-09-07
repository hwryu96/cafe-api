package com.cafe.quiz.support.configuration

import com.cafe.quiz.support.const.HeaderConstant
import com.cafe.quiz.support.web.dto.MemberContext
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfiguration {
    init {
        SpringDocUtils
            .getConfig()
            .addRequestWrapperToIgnore(MemberContext::class.java)
    }

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

    @Bean
    fun customize() =
        OperationCustomizer { operation, _ ->
            operation.addParametersItem(
                HeaderParameter()
                    .name(HeaderConstant.X_CAFE_MEMBER_ID)
                    .required(false)
                    .description("로그인 회원 정보")
                    .schema(
                        NumberSchema().apply {
                            setDefault(1)
                        },
                    ),
            )
        }
}
