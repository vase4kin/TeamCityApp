package teamcityapp.buildlogic

import org.gradle.api.GradleException
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class R8SmokeTestReportsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `missing reports fail rather than accepting a startup failure`() {
        assertThrows(GradleException::class.java) {
            verifyR8SmokeTestReports(emptyList())
        }
    }

    @Test
    fun `incomplete smoke suite fails`() {
        assertThrows(GradleException::class.java) {
            verifyR8SmokeTestReports(listOf(report(4)))
        }
    }

    @Test
    fun `complete smoke suite passes`() {
        verifyR8SmokeTestReports(listOf(report(5)))
    }

    @Test
    fun `tests across report files are counted together`() {
        verifyR8SmokeTestReports(listOf(report(2), report(3)))
    }

    private fun report(count: Int): File = temporaryFolder.newFile().apply {
        writeText("<testsuite tests=\"$count\"/>")
    }
}
