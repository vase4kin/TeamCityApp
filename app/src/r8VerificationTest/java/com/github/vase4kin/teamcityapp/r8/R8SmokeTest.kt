package com.github.vase4kin.teamcityapp.r8

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.annotation.Keep
import org.junit.runner.RunWith

@Keep
@RunWith(AndroidJUnit4::class)
class R8SmokeTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun jsonAndSerializableModels() = R8SmokeChecks.verifyJsonAndBundles()
    @Test fun persistedAccounts() = R8SmokeChecks.verifyAccounts(context)
    @Test fun retrofitAndDiskCache() = R8SmokeChecks.verifyRetrofitAndCache(context)
    @Test fun eventBusAndTimezoneResources() = R8SmokeChecks.verifyEventBusAndDates()

    @Test fun loginScreenStarts() {
        val intent = Intent().setClassName(context, "com.github.vase4kin.teamcityapp.login.view.LoginActivity")
        ActivityScenario.launch<android.app.Activity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }
}
