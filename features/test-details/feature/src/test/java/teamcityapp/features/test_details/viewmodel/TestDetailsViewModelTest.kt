package teamcityapp.features.test_details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.test_details.repository.TestDetails
import teamcityapp.features.test_details.repository.TestDetailsRepository
import teamcityapp.features.test_details.tracker.TestDetailsTracker

@OptIn(ExperimentalCoroutinesApi::class)
class TestDetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val tracker = mock(TestDetailsTracker::class.java)
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun model(repository: TestDetailsRepository, url: String = "/test") =
        TestDetailsViewModel(SavedStateHandle(mapOf(TEST_URL_KEY to url)), repository, tracker)

    @Test fun loadingThenContentPreservesLiteralText() = runTest(dispatcher) {
        val result = CompletableDeferred<TestDetails>()
        val vm = model(object : TestDetailsRepository {
            override suspend fun details(url: String): TestDetails {
                assertEquals("/test", url)
                return result.await()
            }
        })
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            val text = "<assertion> & failure\nline 2"
            result.complete(TestDetails(text))
            runCurrent()
            assertEquals(TestDetailsUiState.Content(text), awaitItem())
        }
        verify(tracker, times(1)).trackView()
    }

    @Test fun emptyResponseShowsEmptyState() = runTest(dispatcher) {
        val vm = model(object : TestDetailsRepository {
            override suspend fun details(url: String) = TestDetails("")
        })
        runCurrent()
        assertEquals(TestDetailsUiState.Empty, vm.state.value)
    }

    @Test fun errorCanRetryThroughLoadingToContent() = runTest(dispatcher) {
        var calls = 0
        val vm = model(object : TestDetailsRepository {
            override suspend fun details(url: String): TestDetails {
                if (++calls == 1) error("network unavailable")
                return TestDetails("recovered")
            }
        })
        vm.state.test {
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
            assertEquals(TestDetailsUiState.Error, awaitItem())
            vm.retry()
            assertEquals(TestDetailsUiState.Loading, awaitItem())
            runCurrent()
            assertEquals(TestDetailsUiState.Content("recovered"), awaitItem())
        }
        assertEquals(2, calls)
    }

    @Test fun invalidInputDoesNotCallRepository() = runTest(dispatcher) {
        val repository = mock(TestDetailsRepository::class.java)
        val vm = model(repository, "  ")
        runCurrent()
        assertEquals(TestDetailsUiState.InvalidInput, vm.state.value)
        verifyNoInteractions(repository)
    }

    @Test fun retryCancelsPreviousRequest() = runTest(dispatcher) {
        var cancelled = false
        var calls = 0
        val vm = model(object : TestDetailsRepository {
            override suspend fun details(url: String): TestDetails {
                if (++calls == 1) {
                    try { awaitCancellation() } finally { cancelled = true }
                }
                return TestDetails("replacement")
            }
        })
        runCurrent()
        vm.retry()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(TestDetailsUiState.Content("replacement"), vm.state.value)
    }

    @Test fun clearingViewModelCancelsLoadingWithoutShowingError() = runTest(dispatcher) {
        var cancelled = false
        val vm = model(object : TestDetailsRepository {
            override suspend fun details(url: String): TestDetails {
                try { awaitCancellation() } finally { cancelled = true }
            }
        })
        val store = ViewModelStore().apply { put("test", vm) }
        runCurrent()
        store.clear()
        runCurrent()
        assertTrue(cancelled)
        assertEquals(TestDetailsUiState.Loading, vm.state.value)
    }
}
