package com.cafe.quiz.support.jpa.converter

import com.cafe.quiz.support.crypto.CryptoService
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Converter
@Component
class HashConverter
    @Autowired
    constructor(
        private val cryptoService: CryptoService,
    ) : AttributeConverter<String, String> {
        override fun convertToDatabaseColumn(attribute: String?) = cryptoService.hash(attribute)

        override fun convertToEntityAttribute(dbData: String?) = dbData
    }
