package com.tolou.mony.ui.data

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object TransactionCipher {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12
    private const val PREFIX = "enc::"

    // App-local key material; rotate with migration if changed.
    private const val SECRET_SEED = "MonySecureTxnV1_2026"

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return plainText
        return runCatching {
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = randomIv()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val payload = iv + encrypted
            PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
        }.getOrDefault(plainText)
    }

    fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(PREFIX)) return cipherText
        return runCatching {
            val payload = Base64.decode(cipherText.removePrefix(PREFIX), Base64.NO_WRAP)
            if (payload.size <= IV_LENGTH_BYTES) return cipherText
            val iv = payload.copyOfRange(0, IV_LENGTH_BYTES)
            val encrypted = payload.copyOfRange(IV_LENGTH_BYTES, payload.size)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        }.getOrDefault(cipherText)
    }

    private fun secretKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256").digest(SECRET_SEED.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest.copyOf(16), KEY_ALGORITHM)
    }

    private fun randomIv(): ByteArray {
        val iv = ByteArray(IV_LENGTH_BYTES)
        java.security.SecureRandom().nextBytes(iv)
        return iv
    }
}
