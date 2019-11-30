/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.test_details.viewmodel

import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import tr.xip.errorview.ErrorView
import javax.inject.Inject

class TestDetailsViewModel @Inject constructor(
    private val dataManager: TestDetailsDataManager,
    private val tracker: TestDetailsTracker,
    private val url: String,
    val finish: () -> Unit
) : ErrorView.RetryListener, LifecycleObserver {

    val progressVisibility = ObservableInt(View.GONE)
    val testDetailsVisibility = ObservableInt(View.GONE)
    val errorVisibility = ObservableInt(View.GONE)
    val emptyVisibility = ObservableInt(View.GONE)
    val testDetailsText = ObservableField<String>("")

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        loadData()
        tracker.trackView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onRetry() {
        errorVisibility.set(View.GONE)
        loadData()
    }

    /**
     * Load test details data
     */
    private fun loadData() {
        if (url.isEmpty()) {
            // Show toast!
            finish()
            return
        }
        progressVisibility.set(View.VISIBLE)
        dataManager.loadData(
            onSuccess = { testDetails ->
                progressVisibility.set(View.GONE)
                if (testDetails.isEmpty()) {
                    emptyVisibility.set(View.VISIBLE)
                } else {
                    testDetailsVisibility.set(View.VISIBLE)
                    testDetailsText.set(testDetails)
                }
            },
            onError = {
                progressVisibility.set(View.GONE)
                errorVisibility.set(View.VISIBLE)
            },
            url = url
        )
    }
}
