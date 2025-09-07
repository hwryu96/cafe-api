package com.cafe.quiz.support.configuration

import com.cafe.quiz.support.const.CafeConstant
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(basePackages = [CafeConstant.BASE_PACKAGE])
class PropertyConfiguration
