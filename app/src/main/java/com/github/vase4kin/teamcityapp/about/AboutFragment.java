/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.github.vase4kin.teamcityapp.BuildConfig;
import com.github.vase4kin.teamcityapp.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.joanzapata.iconify.fonts.MaterialIcons;

public class AboutFragment extends MaterialAboutFragment {

    @Override
    protected MaterialAboutList getMaterialAboutList(final Context context) {
        MaterialAboutCard.Builder appCardBuilder = new MaterialAboutCard.Builder();
        appCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.version))
                .icon(new IconDrawable(context, MaterialIcons.md_info_outline).colorRes(R.color.sub_text_color).sizeDp(24))
                .subText(BuildConfig.VERSION_NAME)
                .build())
                .addItem(ConvenienceBuilder.createRateActionItem(context, new IconDrawable(context, MaterialIcons.md_star_border).colorRes(R.color.sub_text_color).sizeDp(24),
                        getString(R.string.about_app_text_rate_app), null))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_app_text_found_issue)
                        .subText(R.string.about_app_subtext_found_issue)
                        .icon(new IconDrawable(context, MaterialIcons.md_question_answer).colorRes(R.color.sub_text_color).sizeDp(24))
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(getString(R.string.about_app_url_found_issue)));
                                getActivity().startActivity(intent);

                            }
                        })
                        .build());

        MaterialAboutCard.Builder miscCardBuilder = new MaterialAboutCard.Builder();
        miscCardBuilder
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_app_text_source_code)
                        .icon(new IconDrawable(context, MaterialCommunityIcons.mdi_github_circle).colorRes(R.color.sub_text_color).sizeDp(24))
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(getString(R.string.about_app_url_source_code)));
                                getActivity().startActivity(intent);
                            }
                        })
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_app_text_libraries)
                        .icon(new IconDrawable(context, MaterialCommunityIcons.mdi_github_circle).colorRes(R.color.sub_text_color).sizeDp(24))
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                AboutLibrariesActivity.start(getActivity());
                            }
                        })
                        .build());

        MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
        authorCardBuilder.title(R.string.about_app_text_contacts);
        authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                .text(R.string.about_app_text_web)
                .subText(R.string.about_app_url_web)
                .icon(new IconDrawable(context, MaterialCommunityIcons.mdi_web).colorRes(R.color.sub_text_color).sizeDp(24))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.about_app_url_web)));
                        getActivity().startActivity(intent);
                    }
                })
                .build())
                .addItem(ConvenienceBuilder.createEmailItem(context, new IconDrawable(context, MaterialIcons.md_email).colorRes(R.color.sub_text_color).sizeDp(24),
                        getText(R.string.about_app_text_email), true, getString(R.string.about_app_email), getString(R.string.about_app_email_title)));

        return new MaterialAboutList(appCardBuilder.build(), miscCardBuilder.build(), authorCardBuilder.build());
    }

    @Override
    protected int getTheme() {
        return R.style.AppTheme_MaterialAboutActivity_Fragment;
    }

}
