package com.cafe.quiz.feature.member.entity

import com.cafe.quiz.feature.member.model.GenderType
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
import java.time.LocalDateTime

@Entity
@Table(name = "archive_member")
class ArchiveMemberEntity(
    @Id
    val id: Long,
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
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime,
    @Column(name = "archived_at", nullable = false)
    var archivedAt: LocalDateTime = LocalDateTime.now(),
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

    companion object {
        fun from(member: MemberEntity): ArchiveMemberEntity =
            ArchiveMemberEntity(
                id = member.id,
                name = member.name,
                phone = member.phone,
                birth = member.birth,
                gender = member.gender,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt,
            )
    }

    fun toMember() =
        MemberEntity(
            id = this.id,
            name = this.name,
            phone = this.phone,
            birth = this.birth,
            gender = this.gender,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
