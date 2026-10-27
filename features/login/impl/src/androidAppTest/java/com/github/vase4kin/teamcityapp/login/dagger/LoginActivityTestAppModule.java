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

package com.github.vase4kin.teamcityapp.login.dagger;

import android.content.Context;
import androidx.annotation.VisibleForTesting;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;
import org.greenrobot.eventbus.EventBus;
import teamcityapp.libraries.onboarding.OnboardingManager;
import teamcityapp.libraries.onboarding.OnboardingManagerImpl;
import teamcityapp.libraries.security.CryptoManager;
import teamcityapp.libraries.storage.Storage;

/** Todo: Convert to Kotlin */
@Module
@dagger.hilt.migration.DisableInstallInCheck
public class LoginActivityTestAppModule {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @Singleton
    protected Context provideContext(@ApplicationContext Context context) {
        return context;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @Singleton
    protected SharedUserStorage provideSharedUserStorage(
            @ApplicationContext Context context, CryptoManager cryptoManager) {
        return SharedUserStorage.init(context, cryptoManager);
    }

    @Provides
    @Singleton
    protected Storage provideStorage(SharedUserStorage sharedUserStorage) {
        return sharedUserStorage;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @Singleton
    protected EventBus providesEventBus() {
        return EventBus.getDefault();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    @Provides
    @Singleton
    protected OnboardingManager providesOnboardingManager(@ApplicationContext Context context) {
        return new OnboardingManagerImpl(context);
    }
}
