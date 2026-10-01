package com.github.vase4kin.teamcityapp.dagger

import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import teamcityapp.libraries.utils.requireScreenOwner

class ScreenOwnerTest {
    private class HomeScreen
    private class OtherScreen

    @Test
    fun resolvesOnlyTheExpectedScreenOwner() {
        val home = HomeScreen()
        assertSame(home, home.requireScreenOwner<HomeScreen>())
        val error = assertThrows(IllegalStateException::class.java) {
            OtherScreen().requireScreenOwner<HomeScreen>()
        }
        assertTrue(error.message!!.contains(HomeScreen::class.java.name))
        assertTrue(error.message!!.contains(OtherScreen::class.java.name))
    }
}
