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

package teamcityapp.features.run_build.impl
import android.app.Activity
import android.content.Intent
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.features.run_build.impl.navigation.RunBuildNavigationImpl
import teamcityapp.features.run_build.impl.router.RunBuildRouterImpl
import teamcityapp.features.run_build.impl.tracker.RunBuildTrackerImpl
import teamcityapp.libraries.resources.R as SharedR
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class RunBuildOwnershipTest {
    @Test fun launchPreservesRequestCodeExtraAndTransition() {
        val owner = spy(Robolectric.buildActivity(Activity::class.java).setup().get())
        doNothing().`when`(owner).startActivityForResult(any(Intent::class.java), anyInt())
        RunBuildNavigationImpl().openForResult(owner, "build-type")
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        verify(owner).startActivityForResult(intent.capture(), eq(489))
        assertEquals(RunBuildActivity::class.java.name, intent.value.component!!.className)
        assertEquals("build-type", intent.value.getStringExtra("BuildTypeId"))
        verify(owner).overridePendingTransition(SharedR.anim.slide_in_bottom, SharedR.anim.hold)
    }

    @Test fun successReturnsCompatiblePayloadBeforeFinishing() {
        val owner = mock(RunBuildActivity::class.java)
        val router = RunBuildRouterImpl(owner)
        router.queued("queued/href")
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        val order = inOrder(owner)
        order.verify(owner).setResult(eq(Activity.RESULT_OK), intent.capture())
        assertEquals("queued/href", intent.value.getStringExtra("href"))
        order.verify(owner).finish()
        order.verifyNoMoreInteractions()
    }

    @Test fun cancellationReturnsAnEmptyResult() {
        val owner = mock(RunBuildActivity::class.java)
        RunBuildRouterImpl(owner).close()
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        val order = inOrder(owner)
        order.verify(owner).setResult(eq(Activity.RESULT_CANCELED), intent.capture())
        assertNull(intent.value.extras)
        order.verify(owner).finish()
        order.verifyNoMoreInteractions()
    }

    @Test fun analyticsPreserveLegacyEvents() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = RunBuildTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackUserRunBuildSuccess()
        tracker.trackUserRunBuildWithCustomParamsSuccess()
        tracker.trackUserRunBuildFailed()
        tracker.trackUserRunBuildFailedForbidden()
        tracker.trackUserClicksOnAddNewBuildParamButton()
        tracker.trackUserClicksOnClearAllBuildParamsButton()
        tracker.trackUserAddsBuildParam()
        val order = inOrder(analytics)
        order.verify(analytics).logEvent("screen_run_build", null)
        order.verify(analytics).logEvent("run_build_success", null)
        order.verify(analytics).logEvent("run_build_custom_params_success", null)
        order.verify(analytics).logEvent("run_build_failed", null)
        order.verify(analytics).logEvent("run_build_forbidden_error", null)
        order.verify(analytics).logEvent("run_build_add_custom_param_click", null)
        order.verify(analytics).logEvent("run_build_clear_all_params", null)
        order.verify(analytics).logEvent("run_build_add_custom_param", null)
        order.verifyNoMoreInteractions()
    }
}
