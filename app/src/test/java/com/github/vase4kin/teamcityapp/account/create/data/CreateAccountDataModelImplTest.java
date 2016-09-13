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

package com.github.vase4kin.teamcityapp.account.create.data;

import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(PowerMockRunner.class)
public class CreateAccountDataModelImplTest {

    @Mock
    private SharedUserStorage mSharedUserStorage;

    private CreateAccountDataModelImpl mDataModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mDataModel = new CreateAccountDataModelImpl(mSharedUserStorage);
    }

    @Test
    public void testHasAccountWithUrl() throws Exception {
        mDataModel.hasAccountWithUrl("email");
        verify(mSharedUserStorage).hasAccountWithUrl(eq("email"));
        verifyNoMoreInteractions(mSharedUserStorage);
    }
}