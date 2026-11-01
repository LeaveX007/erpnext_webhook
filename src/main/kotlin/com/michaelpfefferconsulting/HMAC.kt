package com.michaelpfefferconsulting

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun generateHmacSha256(data: String, key: String): String {
    val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val digest = mac.doFinal(data.toByteArray())
    return digest.fold("") { str, byte -> str + "%02x".format(byte) }
}