package com.github.vase4kin.teamcityapp.dagger

import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

class ApiSessionTest {
    private val storage = mock(Storage::class.java)
    private val graphs = mutableListOf<RestApiComponent>()
    private val session = ApiSession({ storage }) {
        mock(RestApiComponent::class.java).also(graphs::add)
    }

    private fun account(url: String = "https://teamcity.example/", name: String = "user", password: String = "password") =
        UserAccount(url, name, password.toByteArray(), false, true)

    @Test
    fun initializesLazilyAndReusesSameAccountGraph() {
        `when`(storage.activeUser).thenReturn(account())
        assertEquals(0, graphs.size)
        org.mockito.Mockito.verifyNoInteractions(storage)
        val graph = session.requireGraph()
        assertEquals(1, graphs.size)
        `when`(storage.activeUser).thenReturn(account())
        assertSame(graph, session.requireGraph())
    }

    @Test
    fun switchingUsersOnTheSameServerReplacesTheGraph() {
        `when`(storage.activeUser).thenReturn(account())
        val previous = session.requireGraph()
        `when`(storage.activeUser).thenReturn(account(name = "other"))
        assertNotSame(previous, session.requireGraph())
    }

    @Test
    fun changedCredentialsAndSslSettingsReplaceTheGraph() {
        `when`(storage.activeUser).thenReturn(account())
        val previous = session.requireGraph()
        val updated = account(password = "new-password")
        `when`(storage.activeUser).thenReturn(updated)
        val changedPassword = session.requireGraph()
        assertNotSame(previous, changedPassword)
        updated.isSslDisabled = true
        assertNotSame(changedPassword, session.requireGraph())
    }

    @Test
    fun loggedOutResolutionClearsThePreviousGraph() {
        `when`(storage.activeUser).thenReturn(account())
        val previous = session.requireGraph()
        `when`(storage.activeUser).thenReturn(account(url = ""))
        assertThrows(IllegalStateException::class.java) { session.requireGraph() }
        `when`(storage.activeUser).thenReturn(account())
        assertNotSame(previous, session.requireGraph())
    }

    @Test
    fun explicitLogoutDropsTheGraphEvenWhenTheSameUserReturns() {
        `when`(storage.activeUser).thenReturn(account())
        val previous = session.requireGraph()
        session.clear()
        assertNotSame(previous, session.requireGraph())
    }

    @Test
    fun rejectsAUrlThatDoesNotBelongToTheActiveAccount() {
        `when`(storage.activeUser).thenReturn(account())
        assertThrows(IllegalArgumentException::class.java) { session.rebuild("https://other.example/") }
    }
    @Test
    fun resolvesStorageFromTheCurrentHiltGraph() {
        val replacementStorage = mock(Storage::class.java)
        `when`(storage.activeUser).thenReturn(account())
        `when`(replacementStorage.activeUser).thenReturn(account(name = "other"))
        var currentStorage = storage
        val changingSession = ApiSession({ currentStorage }) { mock(RestApiComponent::class.java) }
        val previous = changingSession.requireGraph()
        currentStorage = replacementStorage
        assertNotSame(previous, changingSession.requireGraph())
    }

}
