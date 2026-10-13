package com.github.vase4kin.teamcityapp.r8

import android.content.Intent
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
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

    @Test fun ossLicensesResourcesAndScreenSurviveShrinking() {
        for (name in listOf("third_party_licenses", "third_party_license_metadata")) {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            assertTrue("Missing OSS licenses resource: $name", id != 0)
            context.resources.openRawResource(id).use {
                assertTrue("Empty OSS licenses resource: $name", it.read() != -1)
            }
        }
        for (name in listOf("libraries_social_licenses_license_menu_activity", "libraries_social_licenses_license_activity", "libraries_social_licenses_license")) {
            assertTrue("Missing OSS licenses layout: $name", context.resources.getIdentifier(name, "layout", context.packageName) != 0)
        }
        val intent = Intent().setClassName(context, "com.google.android.gms.oss.licenses.OssLicensesMenuActivity")
        ActivityScenario.launch<android.app.Activity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }
}
