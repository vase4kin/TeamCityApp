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

package com.github.vase4kin.teamcityapp.drawer.data;

import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

public class DrawerDataModelImplTest {

    @Mock
    private UserAccount mUser;

    private DrawerDataModelImpl mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<UserAccount> users = new ArrayList<>();
        users.add(mUser);
        mDataModel = new DrawerDataModelImpl(users);
    }

    @Test
    public void testGetName() throws Exception {
        when(mUser.getUserName()).thenReturn("name");
        assertThat(mDataModel.getName(0), is(equalTo("name")));
    }

    @Test
    public void testGetTeamCityUrl() throws Exception {
        when(mUser.getTeamcityUrl()).thenReturn("url");
        assertThat(mDataModel.getTeamCityUrl(0), is(equalTo("url")));
    }

    @Test
    public void testIsEmpty() throws Exception {
        mDataModel = new DrawerDataModelImpl(Collections.<UserAccount>emptyList());
        assertThat(mDataModel.isEmpty(), is(equalTo(true)));
    }
}