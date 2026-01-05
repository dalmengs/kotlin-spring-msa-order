package com.dalmeng.convention.common.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.security.SecureRandom
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    val seq: Long = 0L,

    @Column(name = "id", nullable = false, unique = true, updatable = false, length = 26)
    val id: String = generateId(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private val secureRandom = SecureRandom()
        private const val CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

        /**
         * 26자 문자열 ID (ULID 형식과 동일한 길이의 Base32).
         * - DB 컬럼 length(26)과 정합성을 맞추기 위해 16바이트(128bit)를 Base32(5bit)로 인코딩합니다.
         * - 시간 기반 정렬이 필요하면 추후 timestamp 기반 ULID로 확장하세요.
         */
        private fun generateId(): String {
            val bytes = ByteArray(16)
            secureRandom.nextBytes(bytes)
            return encodeCrockfordBase32(bytes)
        }

        private fun encodeCrockfordBase32(bytes: ByteArray): String {
            val out = StringBuilder(26)
            var buffer = 0
            var bitsLeft = 0

            for (b in bytes) {
                buffer = (buffer shl 8) or (b.toInt() and 0xFF)
                bitsLeft += 8
                while (bitsLeft >= 5) {
                    val index = (buffer shr (bitsLeft - 5)) and 0x1F
                    bitsLeft -= 5
                    out.append(CROCKFORD_BASE32[index])
                }
            }

            if (bitsLeft > 0) {
                val index = (buffer shl (5 - bitsLeft)) and 0x1F
                out.append(CROCKFORD_BASE32[index])
            }

            // 128bit -> Base32(5bit) => 26 chars. (padding 없이 정확히 26자를 맞춥니다)
            while (out.length < 26) out.append('0')
            if (out.length > 26) out.setLength(26)
            return out.toString()
        }
    }
}