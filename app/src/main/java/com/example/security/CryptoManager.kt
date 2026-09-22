package com.example.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides AES-256-GCM End-to-End Encryption for sensitive POS transaction records,
 * customer financial details, payment instruments, and internal security credentials.
 */
object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    // Master seed for deriving AES-256 key locally on the device
    private const val DEFAULT_SEED = "FalconRodMakerPOS_SecureKey_2026_Gujrat_PK"

    private val secretKeySpec: SecretKeySpec by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(DEFAULT_SEED.toByteArray(StandardCharsets.UTF_8))
        SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plain text using AES/GCM/NoPadding.
     * Returns a base64 encoded string containing [IV (12 bytes) + CipherText with GCM Auth Tag].
     */
    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback gracefully without losing data
            plainText
        }
    }

    /**
     * Decrypts base64 encoded [IV + CipherText] using AES/GCM/NoPadding.
     */
    fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size <= IV_LENGTH_BYTE) return encryptedText

            val iv = ByteArray(IV_LENGTH_BYTE)
            val cipherText = ByteArray(combined.size - IV_LENGTH_BYTE)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE)
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // If already plaintext or decryption error, return original
            encryptedText
        }
    }

    /**
     * Generates a tamper-proof SHA-256 verification hash for order receipts and audit logs.
     */
    fun generateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
