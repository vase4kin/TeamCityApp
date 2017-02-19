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

package com.github.vase4kin.teamcityapp.overview.data;

import android.content.Context;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class OverviewDataModelImplTest {

    @Mock
    private BuildElement mElement;
    @Mock
    private Context mContext;

    private OverviewDataModelImpl mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<BuildElement> elements = new ArrayList<>();
        elements.add(mElement);
        mDataModel = new OverviewDataModelImpl(elements, mContext);
    }

    @Test
    public void testGetIcon() throws Exception {
        when(mElement.getIcon()).thenReturn("icon");
        assertThat(mDataModel.getIcon(0), is("icon"));
    }

    @Test
    public void testGetDescription() throws Exception {
        when(mElement.getDescription()).thenReturn("desc");
        assertThat(mDataModel.getDescription(0), is("desc"));
    }

    @Test
    public void testGetSectionName() throws Exception {
        when(mElement.getSectionName()).thenReturn("section");
        assertThat(mDataModel.getHeaderName(0), is("section"));
    }

    @Test
    public void testGetItemCount() throws Exception {
        assertThat(mDataModel.getItemCount(), is(1));
    }

    @Test
    public void testIsBranchCard() throws Exception {
        when(mElement.getSectionName()).thenReturn("section");
        when(mContext.getString(R.string.build_branch_section_text)).thenReturn("section");
        assertThat(mDataModel.isBranchCard(0), is(true));
    }
}