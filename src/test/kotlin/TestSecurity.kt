import com.michaelpfefferconsulting.HMAC
import com.michaelpfefferconsulting.Security
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertTrue

class TestSecurity {

    /*
    * Reference is: https://codeshack.io/hmac-generator/
    * */
    @Test
    fun `Test Check Signature`() {
        val text = "test"
        val key = "12345"
        val hmacSha256 = "e031ba1c2938b1d283f919a1df9ac66c192a7dd74432d5fd4ada299941ba109f"
        val base64Signature = "4DG6HCk4sdKD+Rmh35rGbBkqfddEMtX9StopmUG6EJ8="

        val hmacMock = mock<HMAC> {
            on { generateHmacSha256(text, key) } doReturn hmacSha256
        }

        val security = Security(hmacMock)
        val isValid = security.checkSignature(base64Signature, text, key)

        assertTrue(isValid)
    }

}