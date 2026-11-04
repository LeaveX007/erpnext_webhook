package com.michaelpfefferconsulting

import kotlin.io.encoding.Base64

class Security(
    private val hmac: HMAC
) {

    fun getBase64Signature(text: String, key: String): String {
        val hmacSha256 = hmac.generateHmacSha256(text, key)
        return encodeToBase64(hmacSha256)
    }

    fun checkSignature(base64SignatureToCheck: String, text: String, key: String): Boolean {
        val base64Signature = getBase64Signature(text, key)
        return base64SignatureToCheck.contentEquals(base64Signature)
    }

    private fun encodeToBase64(hmacSha256: String): String {
        val decodedBytes = hmacSha256.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        return Base64.encode(decodedBytes)
    }

}