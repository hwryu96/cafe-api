package com.cafe.quiz.feature.member.entity

import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.support.generator.IdGenerator
import com.cafe.quiz.support.jpa.converter.CryptoConverter
import com.cafe.quiz.support.jpa.converter.HashConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
class MemberEntity(
    @Id
    val id: Long = IdGenerator.generate(),
    @Convert(converter = CryptoConverter::class)
    @Column(name = "name_enc", nullable = false, length = 512)
    val name: String,
    @Convert(converter = CryptoConverter::class)
    @Column(name = "phone_enc", nullable = false, length = 512)
    val phone: String,
    @Convert(converter = CryptoConverter::class)
    @Column(name = "birth_enc", nullable = false, length = 512)
    var birth: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 6)
    val gender: GenderType,
    /**
     * 삭제 여부는 분리보관 프로세스 처리 전 임시로 사용한다.
     * 분리보관이 완료 된 경우에는 row 자체가 제거된다.
     */
    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(name = "name_hash", nullable = false, length = 64)
    @Convert(converter = HashConverter::class)
    lateinit var nameHash: String

    @Column(name = "phone_hash", nullable = false, length = 64)
    @Convert(converter = HashConverter::class)
    lateinit var phoneHash: String

    @PrePersist
    @PreUpdate
    fun syncEncryptedFields() {
        this.nameHash = name
        this.phoneHash = phone
    }
}
