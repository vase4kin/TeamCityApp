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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import teamcityapp.features.about.repository.AboutRepository
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.utils.getThemeColor
import teamcityapp.libraries.utils.getTintedDrawable
import javax.inject.Inject

class AboutFragment : MaterialAboutFragment() {

    @Inject
    lateinit var repository: AboutRepository

    @Inject
    lateinit var chromeCustomTabs: ChromeCustomTabs

    private var listener: AboutActivityLoadingListener? = null
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
        if (context is AboutActivityLoadingListener) {
            listener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chromeCustomTabs.initCustomsTabs()
        loadServerInfo()
    }

    override fun onDestroyView() {
        subscriptions.clear()
        chromeCustomTabs.unbindCustomsTabs()
        super.onDestroyView()
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList = MaterialAboutList()

    private fun loadServerInfo() {
        repository.serverInfo().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { listener?.showLoader() }
            .doFinally { listener?.hideLoader() }
            .subscribeBy(
                onSuccess = {
                    val serverInfo = createServerInfoCard(it.version, it.webUrl)
                    val materialAboutList = createMaterialAboutList(requireActivity(), serverInfo)
                    list.cards.clear()
                    list.cards.addAll(materialAboutList.cards)
                    refreshMaterialAboutList()
                },
                onError = {
                    val materialAboutList = createMaterialAboutList(requireActivity())
                    list.cards.clear()
                    list.cards.addAll(materialAboutList.cards)
                    refreshMaterialAboutList()
                }
            )
            .addTo(subscriptions)
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
                .subText(BuildConfig.VERSION_NAME)
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
