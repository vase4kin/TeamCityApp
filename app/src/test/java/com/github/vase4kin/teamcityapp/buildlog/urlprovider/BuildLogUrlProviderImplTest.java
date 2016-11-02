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

package com.github.vase4kin.teamcityapp.buildlog.urlprovider;

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BuildLogUrlProviderImplTest {

    @Mock
    private BuildLogValueExtractor mValueExtractor;

    @Mock
    private UserAccount mUserAccount;

    private BuildLogUrlProviderImpl mBuildLogUrlProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mBuildLogUrlProvider = new BuildLogUrlProviderImpl(mValueExtractor, mUserAccount);
        when(mValueExtractor.getBuildId()).thenReturn("1234");
        when(mUserAccount.getTeamcityUrl()).thenReturn("http://fake-url.com");
    }

    @Test
    public void testProvideUrlForGuestAccount() throws Exception {
        when(mUserAccount.isGuestUser()).thenReturn(true);
        assertThat(mBuildLogUrlProvider.provideUrl(), is(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog&guest=1")));
    }

    @Test
    public void testProvideUrlForUserAccount() throws Exception {
        when(mUserAccount.isGuestUser()).thenReturn(false);
        assertThat(mBuildLogUrlProvider.provideUrl(), is(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog")));
    }
}