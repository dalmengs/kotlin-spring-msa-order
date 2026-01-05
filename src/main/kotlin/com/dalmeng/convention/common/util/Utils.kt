package com.dalmeng.convention.common.util

import java.security.SecureRandom

class Utils {
    companion object {
        private val secureRandom = SecureRandom()
        private const val CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

        fun generateRandomId(): String {
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

            while (out.length < 26) out.append('0')
            if (out.length > 26) out.setLength(26)
            return out.toString()
        }
    }
}