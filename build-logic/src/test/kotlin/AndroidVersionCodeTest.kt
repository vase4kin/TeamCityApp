package teamcityapp.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidVersionCodeTest {
    @Test
    fun `current version exceeds the previous published version code`() {
        assertEquals(15_208, androidVersionCode("1.52.8"))
        assertEquals(androidVersionCode(Config.versionName), Config.versionCode)
        assertTrue(Config.versionCode > 117)
    }

    @Test
    fun `version codes increase across patch minor and major releases`() {
        listOf(
            "1.52.8" to "1.52.9",
            "1.52.99" to "1.53.0",
            "1.99.99" to "2.0.0-beta.0",
            "2.0.0-beta.0" to "2.0.0-beta.1",
            "2.0.0-beta.1" to "2.0.0-beta.2",
            "2.0.0-beta.9" to "2.0.0-beta.10",
            "2.0.0-beta.998" to "2.0.0",
            "2.0.0" to "2.0.1-beta.0",
            "2.0.99" to "2.1.0-beta.0",
            "2.99.99" to "3.0.0-beta.0"
        ).forEach { (previous, next) ->
            assertTrue(androidVersionCode(next) > androidVersionCode(previous))
        }
    }

    @Test
    fun `beta builds have distinct version codes below the stable release`() {
        assertEquals(20_000_000, androidVersionCode("2.0.0-beta.0"))
        assertEquals(20_000_001, androidVersionCode("2.0.0-beta.1"))
        assertEquals(20_000_002, androidVersionCode("2.0.0-beta.2"))
        assertEquals(20_000_999, androidVersionCode("2.0.0"))
    }

    @Test
    fun `maximum supported version fits the Play Store limit`() {
        val versionCode = androidVersionCode("209.99.99")
        assertEquals(2_099_999_999, versionCode)
        assertTrue(versionCode < 2_100_000_000)
    }

    @Test
    fun `invalid versions fail instead of producing conflicting version codes`() {
        listOf(
            "1.52", "1.52.8.1", "1.52.8-beta.1", "a.52.8",
            "0.52.8", "210.0.0", "1.100.0", "1.0.100", "1.-1.0", "1.0.-1",
            "2.0.0-beta", "2.0.0-beta.-1", "2.0.0-beta.01", "2.0.0-beta.999",
            "2.0.0-rc.0", "2.0.0-beta.0+build", "2.0.0-beta.9999999999999999999",
            "9999999999999999999.0.0"
        ).forEach { version ->
            assertThrows(IllegalArgumentException::class.java) {
                androidVersionCode(version)
            }
        }
    }
}
