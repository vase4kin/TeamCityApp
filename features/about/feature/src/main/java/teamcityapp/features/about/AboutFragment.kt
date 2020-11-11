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

package teamcityapp.features.about

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.elevation.ElevationOverlayProvider
import dagger.android.support.AndroidSupportInjection
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.utils.getThemeColor
import teamcityapp.libraries.utils.getTintedDrawable
import javax.inject.Inject

class AboutFragment : MaterialAboutFragment() {

    companion object {

        private const val ARG_SERVER_INFO = "arg_server_info"

        fun create(serverInfo: ServerInfo?): AboutFragment {
            return AboutFragment().also {
                it.arguments = Bundle().apply {
                    putParcelable(ARG_SERVER_INFO, serverInfo)
                }
            }
        }
    }

    @Inject
    lateinit var chromeCustomTabs: ChromeCustomTabs

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chromeCustomTabs.initCustomsTabs()
    }

    override fun onDestroyView() {
        chromeCustomTabs.unbindCustomsTabs()
        super.onDestroyView()
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList = loadList()

    private fun loadList(): MaterialAboutList {
        val serverInfo: ServerInfo? = arguments?.getParcelable(ARG_SERVER_INFO)
        val serverInfoCard = if (serverInfo == null) {
            null
        } else {
            createServerInfoCard(serverInfo.version, serverInfo.webUrl)
        }
        return createMaterialAboutList(requireActivity(), serverInfoCard)
    }

    private fun createServerInfoCard(
        version: String,
        serverUrl: String
    ): MaterialAboutCard {
        val serverInfo = MaterialAboutCard.Builder()
        serverInfo.title(R.string.about_app_text_server_info)
        serverInfo.cardColor(getBackgroundColor(requireActivity()))
        serverInfo.addItem(
            MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_version))
                .icon(
                    getTintedDrawable(
                        requireContext(),
                        R.drawable.ic_info_outline_black_24dp,
                        R.color.material_on_surface_emphasis_medium
                    )
                )
                .subText(version)
                .build()
        )
        val serverUrlItem = MaterialAboutActionItem.Builder()
            .text(R.string.about_app_text_server_url)
            .subText(serverUrl)
            .icon(
                getTintedDrawable(
                    requireContext(),
                    R.drawable.ic_web_black_24dp,
                    R.color.material_on_surface_emphasis_medium
                )
            )
            .setOnClickAction {
                openUrl(serverUrl)
            }
        serverInfo.addItem(serverUrlItem.build())
        return serverInfo.build()
    }

    private fun createMaterialAboutList(
        activity: Activity,
        serverCard: MaterialAboutCard? = null
    ): MaterialAboutList {
        val appCardBuilder = MaterialAboutCard.Builder()
        appCardBuilder.title(R.string.about_app_text_app)
            .cardColor(getBackgroundColor(requireActivity()))
        appCardBuilder.addItem(
            MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_version))
                .icon(
                    getTintedDrawable(
                        requireContext(),
                        R.drawable.ic_info_outline_black_24dp,
                        R.color.material_on_surface_emphasis_medium
                    )
                )
                .subText(BuildConfig.VERSION)
                .build()
        )
            .addItem(
                ConvenienceBuilder.createRateActionItem(
                    activity,
                    getTintedDrawable(
                        requireContext(),
                        R.drawable.ic_star_border_black_24dp,
                        R.color.material_on_surface_emphasis_medium
                    ),
                    getString(R.string.about_app_text_rate_app),
                    null
                )
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.about_app_text_found_issue)
                    .subText(R.string.about_app_subtext_found_issue)
                    .icon(
                        getTintedDrawable(
                            requireContext(),
                            R.drawable.ic_question_answer_black_24dp,
                            R.color.material_on_surface_emphasis_medium
                        )
                    )
                    .setOnClickAction {
                        val url = getString(R.string.about_app_url_found_issue)
                        openUrl(url)
                    }
                    .build()
            )

        val miscCardBuilder = MaterialAboutCard.Builder()
        miscCardBuilder.title(R.string.about_app_text_dev)
            .cardColor(getBackgroundColor(requireActivity()))
        miscCardBuilder
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.about_app_text_source_code)
                    .icon(
                        getTintedDrawable(
                            requireContext(),
                            R.drawable.ic_github_circle,
                            R.color.material_on_surface_emphasis_medium
                        )
                    )
                    .setOnClickAction {
                        val url = getString(R.string.about_app_url_source_code)
                        openUrl(url)
                    }
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.about_app_text_libraries)
                    .icon(
                        getTintedDrawable(
                            requireContext(),
                            R.drawable.ic_github_circle,
                            R.color.material_on_surface_emphasis_medium
                        )
                    )
                    .setOnClickAction {
                        OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_app_text_libraries))
                        startActivity(
                            Intent(
                                requireContext(),
                                OssLicensesMenuActivity::class.java
                            )
                        )
                    }
                    .build()
            )

        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title(R.string.about_app_text_contacts)
            .cardColor(getBackgroundColor(requireActivity()))
        authorCardBuilder.addItem(
            MaterialAboutActionItem.Builder()
                .text(R.string.about_app_text_web)
                .subText(R.string.about_app_url_web)
                .icon(
                    getTintedDrawable(
                        requireContext(),
                        R.drawable.ic_web_black_24dp,
                        R.color.material_on_surface_emphasis_medium
                    )
                )
                .setOnClickAction {
                    val url = getString(R.string.about_app_url_web)
                    openUrl(url)
                }
                .build()
        )
            .addItem(
                ConvenienceBuilder.createEmailItem(
                    activity,
                    getTintedDrawable(
                        requireContext(),
                        R.drawable.ic_email_black_24dp,
                        R.color.material_on_surface_emphasis_medium
                    ),
                    getText(R.string.about_app_text_email),
                    true,
                    getString(R.string.about_app_email),
                    getString(R.string.about_app_email_title)
                )
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.about_app_text_privacy)
                    .icon(
                        getTintedDrawable(
                            requireContext(),
                            R.drawable.ic_web_black_24dp,
                            R.color.material_on_surface_emphasis_medium
                        )
                    )
                    .setOnClickAction {
                        val url = getString(R.string.about_app_url_privacy)
                        openUrl(url)
                    }
                    .build()
            )
        return if (serverCard == null) {
            MaterialAboutList(
                appCardBuilder.build(),
                miscCardBuilder.build(),
                authorCardBuilder.build()
            )
        } else {
            MaterialAboutList(
                serverCard,
                appCardBuilder.build(),
                miscCardBuilder.build(),
                authorCardBuilder.build()
            )
        }
    }

    private fun getBackgroundColor(activity: Activity): Int {
        val elevation =
            activity.resources.getDimension(R.dimen.dp_4)
        return ElevationOverlayProvider(activity).compositeOverlayIfNeeded(
            activity.getThemeColor(R.attr.colorSurface), elevation
        )
    }

    private fun openUrl(url: String) {
        chromeCustomTabs.launchUrl(url)
    }
}
