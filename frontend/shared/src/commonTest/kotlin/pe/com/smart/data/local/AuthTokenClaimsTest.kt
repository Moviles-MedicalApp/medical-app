package pe.com.smart.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthTokenClaimsTest {
    @Test
    fun `decodes access token claims used for navigation`() {
        val token = "header.eyJzdWIiOiJqdWFuIiwidXNlcklkIjoxNSwicm9sZXMiOlsiUEFUSUVOVCJdLCJleHAiOjE3OTQ1MjM2MDB9.signature"

        val claims = AccessTokenClaimsDecoder.decode(token)

        assertEquals("juan", claims?.username)
        assertEquals(15, claims?.userId)
        assertTrue(claims?.hasRole("PATIENT") == true)
        assertFalse(claims?.hasRole("ADMIN") == true)
    }

    @Test
    fun `opaque refresh value is not treated as a JWT`() {
        assertEquals(null, AccessTokenClaimsDecoder.decode("opaque-refresh-token"))
    }

    @Test
    fun `normalizes backend role prefix for navigation`() {
        val token = "header.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfQURNSU4iXX0.signature"

        assertTrue(AccessTokenClaimsDecoder.decode(token)?.hasRole("ADMIN") == true)
    }
}
