import to.bnt.bebroo.web.greet
import kotlin.test.Test
import kotlin.test.assertEquals

class TestClient {
    @Test
    fun testGreet() {
        assertEquals("Hello, Web", greet())
    }
}