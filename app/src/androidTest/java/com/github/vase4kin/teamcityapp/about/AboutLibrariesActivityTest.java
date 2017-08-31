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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;

/**
 * Tests for {@link AboutLibrariesActivity}
 */
@RunWith(AndroidJUnit4.class)
public class AboutLibrariesActivityTest {

    private static final String URL = "http://teamcity.server.com";

    @Rule
    public DaggerMockRule<RestApiComponent> mDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomActivityTestRule<AboutLibrariesActivity> mActivityTestRule = new CustomActivityTestRule<>(AboutLibrariesActivity.class);

    @Test
    public void testAboutActivity() throws Exception {
        mActivityTestRule.launchActivity(null);
        String toolbarTitle = mActivityTestRule.getActivity().getString(R.string.about_app_text_libraries);
        matchToolbarTitle(toolbarTitle);
    }
}