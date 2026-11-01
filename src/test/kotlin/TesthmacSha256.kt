package com.michaelpfefferconsulting

import org.junit.Test
import kotlin.test.assertEquals

class TesthmacSha256 {

    @Test
    fun testHmacSha256() {
        val message = "Diesen Text zum Signieren"
        val secret = "SuperGeheimesPasswort"
        val signature = generateHmacSha256(message, secret)
        assertEquals("08f0f09b20145a065b25e6774b6768bb30ee15cc2d6bb2792047c2bedbf83234", signature)
    }

}