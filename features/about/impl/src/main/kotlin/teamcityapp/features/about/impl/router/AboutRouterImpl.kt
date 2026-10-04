/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about.impl.router

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import teamcityapp.features.about.impl.R
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.features.about.impl.AboutAction
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs

class AboutRouterImpl(
    private val activity: Activity,
    private val chromeTabs: ChromeCustomTabs,
    private val appRating: AppRating,
) : AboutRouter {
    override fun start() = chromeTabs.initCustomsTabs()
    override fun stop() = chromeTabs.unbindCustomsTabs()
    override fun openUrl(url: String) = chromeTabs.launchUrl(url)
    override fun close() = activity.finish()

    override fun open(action: AboutAction) {
        when (action) {
            AboutAction.Rate -> appRating.open(activity)
            AboutAction.Issue -> chromeTabs.launchUrl(activity.getString(R.string.about_app_url_found_issue))
            AboutAction.Source -> chromeTabs.launchUrl(activity.getString(R.string.about_app_url_source_code))
            AboutAction.Website -> chromeTabs.launchUrl(activity.getString(R.string.about_app_url_web))
            AboutAction.Privacy -> chromeTabs.launchUrl(activity.getString(SharedR.string.about_app_url_privacy))
            AboutAction.Libraries -> {
                OssLicensesMenuActivity.setActivityTitle(activity.getString(R.string.about_app_text_libraries))
                activity.startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            }
            AboutAction.Email -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${activity.getString(R.string.about_app_email)}"))
                    .putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.about_app_email_title))
                try {
                    activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.about_send_email)))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(activity, R.string.about_no_email_app, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
