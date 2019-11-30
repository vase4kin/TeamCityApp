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

package teamcityapp.features.test_details.utils

import android.webkit.WebView
import androidx.databinding.BindingAdapter
import tr.xip.errorview.ErrorView

@BindingAdapter("testDetails")
fun loadTestDetails(view: WebView, testDetails: String) {
    view.loadDataWithBaseURL(null, testDetails, "text/html", "utf-8", null)
}

@BindingAdapter("retryListener")
fun setRetryListener(errorView: ErrorView, retryListener: ErrorView.RetryListener) {
    errorView.setRetryListener(retryListener)
}
