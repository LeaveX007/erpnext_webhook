package com.michaelpfefferconsulting

import kotlin.io.encoding.Base64

class Security(
    private val hmac: HMAC
) {

    fun checkSignature(base64Signature: String, text: String, key: String): Boolean {
        val hmacSha256 = hmac.generateHmacSha256(text, key)
        val base64 = encodeToBase64(hmacSha256)
        return base64Signature.contentEquals(base64)
    }

    private fun encodeToBase64(hmacSha256: String): String {
        val decodedBytes = hmacSha256.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        return Base64.encode(decodedBytes)
    }

}