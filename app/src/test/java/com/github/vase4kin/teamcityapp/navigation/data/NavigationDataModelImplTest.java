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

package com.github.vase4kin.teamcityapp.navigation.data;

import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.api.Project;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class NavigationDataModelImplTest {

    @Mock
    private NavigationItem mNavigationItem;

    private NavigationDataModel mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<NavigationItem> items = new ArrayList<>();
        items.add(mNavigationItem);
        mDataModel = new NavigationDataModelImpl(items);
    }

    @Test
    public void testGetName() {
        when(mNavigationItem.getName()).thenReturn("name");
        assertThat(mDataModel.getName(0), is("name"));
    }

    @Test
    public void testGetDescription() {
        when(mNavigationItem.getDescription()).thenReturn("desc");
        assertThat(mDataModel.getDescription(0), is("desc"));
    }

    @Test
    public void testIsProject() {
        List<NavigationItem> items = new ArrayList<>();
        items.add(new Project());
        mDataModel = new NavigationDataModelImpl(items);
        assertThat(mDataModel.isProject(0), is(true));
    }

    @Test
    public void testGetNavigationItem() {
        assertThat(mDataModel.getNavigationItem(0), is(mNavigationItem));
    }

    @Test
    public void testGetItemCount() {
        assertThat(mDataModel.getItemCount(), is(1));
    }
}