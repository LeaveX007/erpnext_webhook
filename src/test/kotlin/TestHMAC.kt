import com.michaelpfefferconsulting.HMAC
import kotlin.test.Test
import kotlin.test.assertEquals

class TestHMAC {

    @Test
    fun `Test Generate Hmac Sha 256`() {
        val hmac = HMAC()
        val security = hmac.generateHmacSha256("test", "12345")
        assertEquals("e031ba1c2938b1d283f919a1df9ac66c192a7dd74432d5fd4ada299941ba109f", security)
    }

}