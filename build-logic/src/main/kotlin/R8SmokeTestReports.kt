package teamcityapp.buildlogic

import org.gradle.api.GradleException
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Rejects startup failures that instrumentation can otherwise report as zero successful tests. */
fun verifyR8SmokeTestReports(reports: Collection<File>) {
    val parser = DocumentBuilderFactory.newInstance().apply {
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    }.newDocumentBuilder()
    val testCount = reports.sumOf {
        parser.parse(it).documentElement.getAttribute("tests").toInt()
    }
    if (testCount < 5) {
        throw GradleException("Expected at least 5 R8 smoke tests, but ran $testCount.")
    }
}
