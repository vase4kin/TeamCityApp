/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.libraries.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

class ApiSessionTest {
    private val storage = mock(Storage::class.java)
    private val graphs = mutableListOf<Any>()
    private val session = ApiSession({ storage }) {
        mock(Any::class.java).also(graphs::add)
    }

    private fun account(url: String = "https://teamcity.example/", name: String = "user", password: String = "password") = UserAccount(url, name, password.toByteArray(), false, true)

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
        val changingSession = ApiSession({ currentStorage }) { mock(Any::class.java) }
        val previous = changingSession.requireGraph()
        currentStorage = replacementStorage
        assertNotSame(previous, changingSession.requireGraph())
    }
}
