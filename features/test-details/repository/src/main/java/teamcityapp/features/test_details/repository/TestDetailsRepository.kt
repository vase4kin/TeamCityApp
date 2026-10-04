package teamcityapp.features.test_details.repository

/** Plain domain data; network/cache DTOs stay behind the repository adapter. */
data class TestDetails(val text: String)

interface TestDetailsRepository {
    suspend fun details(url: String): TestDetails
}
