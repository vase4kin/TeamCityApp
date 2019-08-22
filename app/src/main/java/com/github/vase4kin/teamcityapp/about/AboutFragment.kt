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

package com.github.vase4kin.teamcityapp.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.github.vase4kin.teamcityapp.BuildConfig
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialCommunityIcons
import com.joanzapata.iconify.fonts.MaterialIcons
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AboutFragment : MaterialAboutFragment() {

    private val iconSize = 24

    @Inject
    lateinit var repository: Repository

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository.serverInfo().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    updateServerInfoCard(it.version, it.webUrl)
                },
                onError = {
                    removeServerCard()
                }
            )
            .addTo(subscriptions)
    }

    override fun onDestroyView() {
        subscriptions.clear()
        super.onDestroyView()
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val activity = requireActivity()

        val loadingDataString = activity.getString(R.string.about_app_loading)
        val serverInfoCard = createServerInfoCard(loadingDataString, loadingDataString)

        val appCardBuilder = MaterialAboutCard.Builder()
        appCardBuilder.title(R.string.about_app_text_app)
        appCardBuilder.addItem(
            MaterialAboutActionItem.Builder()
                .text(getString(R.string.version))
                .icon(
                    IconDrawable(
                        context,
                        MaterialIcons.md_info_outline
                    ).colorRes(R.color.sub_text_color).sizeDp(
                        iconSize
                    )
                )
                .subText(BuildConfig.VERSION_NAME)
                .build()
        )
            .addItem(
                ConvenienceBuilder.createRateActionItem(
                    context,
                    IconDrawable(
                        context,
                        MaterialIcons.md_star_border
                    ).colorRes(R.color.sub_text_color).sizeDp(iconSize),
                    getString(R.string.about_app_text_rate_app),
                    null
                )
            )
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_app_text_found_issue)
                .subText(R.string.about_app_subtext_found_issue)
                .icon(
                    IconDrawable(
                        context,
                        MaterialIcons.md_question_answer
                    ).colorRes(R.color.sub_text_color).sizeDp(iconSize)
                )
                .setOnClickAction {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(getString(R.string.about_app_url_found_issue))
                    activity.startActivity(intent)
                }
                .build())

        val miscCardBuilder = MaterialAboutCard.Builder()
        miscCardBuilder.title(R.string.about_app_text_dev)
        miscCardBuilder
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_app_text_source_code)
                .icon(
                    IconDrawable(
                        context,
                        MaterialCommunityIcons.mdi_github_circle
                    ).colorRes(R.color.sub_text_color).sizeDp(iconSize)
                )
                .setOnClickAction {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(getString(R.string.about_app_url_source_code))
                    activity.startActivity(intent)
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.about_app_text_libraries)
                .icon(
                    IconDrawable(
                        context,
                        MaterialCommunityIcons.mdi_github_circle
                    ).colorRes(R.color.sub_text_color).sizeDp(iconSize)
                )
                .setOnClickAction { AboutLibrariesActivity.start(activity) }
                .build())

        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title(R.string.about_app_text_contacts)
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text(R.string.about_app_text_web)
            .subText(R.string.about_app_url_web)
            .icon(
                IconDrawable(
                    context,
                    MaterialCommunityIcons.mdi_web
                ).colorRes(R.color.sub_text_color).sizeDp(
                    iconSize
                )
            )
            .setOnClickAction {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.about_app_url_web))
                activity.startActivity(intent)
            }
            .build())
            .addItem(
                ConvenienceBuilder.createEmailItem(
                    context,
                    IconDrawable(
                        context,
                        MaterialIcons.md_email
                    ).colorRes(R.color.sub_text_color).sizeDp(iconSize),
                    getText(R.string.about_app_text_email),
                    true,
                    getString(R.string.about_app_email),
                    getString(R.string.about_app_email_title)
                )
            )

        return MaterialAboutList(
            serverInfoCard,
            appCardBuilder.build(),
            miscCardBuilder.build(),
            authorCardBuilder.build()
        )
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_MaterialAboutActivity_Fragment
    }

    private fun updateServerInfoCard(
        version: String,
        serverUrl: String
    ) {
        list.cards.removeAt(0)
        list.cards.add(0, createServerInfoCard(version, serverUrl))
        refreshMaterialAboutList()
    }

    private fun removeServerCard() {
        list.cards.removeAt(0)
        refreshMaterialAboutList()
    }

    private fun createServerInfoCard(
        version: String,
        serverUrl: String
    ): MaterialAboutCard {
        val activity = requireActivity()
        val serverInfo = MaterialAboutCard.Builder()
        serverInfo.title(R.string.about_app_text_server_info)
        serverInfo.addItem(
            MaterialAboutActionItem.Builder()
                .text(getString(R.string.version))
                .icon(
                    IconDrawable(
                        context,
                        MaterialIcons.md_info_outline
                    ).colorRes(R.color.sub_text_color).sizeDp(
                        iconSize
                    )
                )
                .subText(version)
                .build()
        )
        val serverUrlItem = MaterialAboutActionItem.Builder()
            .text(R.string.about_app_text_server_url)
            .subText(serverUrl)
            .icon(
                IconDrawable(
                    context,
                    MaterialCommunityIcons.mdi_web
                ).colorRes(R.color.sub_text_color).sizeDp(
                    iconSize
                )
            ).setOnClickAction {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(serverUrl)
                activity.startActivity(intent)
            }
        serverInfo.addItem(serverUrlItem.build())
        return serverInfo.build()
    }
}
