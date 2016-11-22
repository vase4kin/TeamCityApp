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

package com.github.vase4kin.teamcityapp.account.create.dagger;

import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter;
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class UrlFormatterModule {

    @Provides
    UrlFormatter providesUrlFormatter() {
        return new UrlFormatterImpl();
    }
}
