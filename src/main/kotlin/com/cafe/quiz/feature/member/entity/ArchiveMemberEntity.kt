package com.cafe.quiz.feature.member.entity

import com.cafe.quiz.feature.member.model.GenderType
import com.cafe.quiz.support.const.CafeConstant
import com.cafe.quiz.support.jpa.converter.CryptoConverter
import com.cafe.quiz.support.jpa.converter.HashConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "archive_member")
class ArchiveMemberEntity(
    @Id
    val id: Long,
    @Convert(converter = CryptoConverter::class)
    @Column(name = "name_enc", length = 512)
    var name: String?,
    @Convert(converter = CryptoConverter::class)
    @Column(name = "phone_enc", length = 512)
    var phone: String?,
    @Convert(converter = CryptoConverter::class)
    @Column(name = "birth_enc", length = 512)
    var birth: String?,
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 6)
    val gender: GenderType,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime,
    @Column(name = "archived_at", nullable = false)
    var archivedAt: LocalDateTime = LocalDateTime.now(),
    /**
     * 해당 필드가 true인 경우에는, 개인정보가 모두 파기되었다.
     */
    @Column(name = "destroyed", nullable = false)
    var destroyed: Boolean = false,
) {
    @Column(name = "name_hash", length = 64)
    @Convert(converter = HashConverter::class)
    lateinit var nameHash: String

    @Column(name = "phone_hash", length = 64)
    @Convert(converter = HashConverter::class)
    lateinit var phoneHash: String

    @PrePersist
    fun syncEncryptedFields() {
        if (!name.isNullOrBlank()) {
            this.nameHash = name!!
        }

        if (!phone.isNullOrBlank()) {
            this.phoneHash = phone!!
        }
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

    fun toMember(): MemberEntity {
        if (destroyed) {
            throw IllegalStateException("이미 데이터 파기됨")
        }

        return MemberEntity(
            id = this.id,
            name = this.name!!,
            phone = this.phone!!,
            birth = this.birth!!,
            gender = this.gender,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }

    // 데이터가 파기되지 않았고, 탈퇴 철회 기간이 지나지 않은 경우에만 복구가 가능하다.
    fun canRestore() = !destroyed && !LocalDateTime.now().isAfter(archivedAt.plusDays(CafeConstant.MEMBER_RESTORE_DAYS))
}
