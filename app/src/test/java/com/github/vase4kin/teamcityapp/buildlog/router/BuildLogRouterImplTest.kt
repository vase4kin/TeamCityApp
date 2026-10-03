package com.github.vase4kin.teamcityapp.buildlog.router

import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs

class BuildLogRouterImplTest {
    @Test
    fun customTabsCanBeBoundForEachViewLifetime() {
        val tabs = mock(ChromeCustomTabs::class.java)
        val router = BuildLogRouterImpl(tabs)
        verifyNoInteractions(tabs)

        router.initCustomsTabs()
        router.openUrl("https://teamcity.example/build/1")
        router.unbindCustomsTabs()
        router.initCustomsTabs()
        router.openUrl("https://teamcity.example/build/1")
        router.unbindCustomsTabs()

        inOrder(tabs).apply {
            verify(tabs).initCustomsTabs()
            verify(tabs).launchUrl("https://teamcity.example/build/1")
            verify(tabs).unbindCustomsTabs()
            verify(tabs).initCustomsTabs()
            verify(tabs).launchUrl("https://teamcity.example/build/1")
            verify(tabs).unbindCustomsTabs()
        }
    }
}
