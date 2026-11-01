package com.michaelpfefferconsulting

import kotlin.io.encoding.Base64

class Security {

    fun checkSignature(signature: String, text: String, key: String): Boolean {
        val hmacSha256 = generateHmacSha256(text, key)
        val base64 = encodeToBase64(hmacSha256)
        return signature.contentEquals(base64)
    }

    private fun encodeToBase64(hmacSha256: String): String {
        return Base64.encode(hmacSha256.encodeToByteArray())
    }

}