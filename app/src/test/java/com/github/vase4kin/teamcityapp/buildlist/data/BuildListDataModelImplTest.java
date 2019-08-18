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

package com.github.vase4kin.teamcityapp.buildlist.data;

import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BuildListDataModelImplTest {

    @Mock
    private Build mBuild;

    @Mock
    private BuildDetails mBuildDetails;

    private BuildListDataModelImpl mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<BuildDetails> buildList = new ArrayList<>();
        buildList.add(mBuildDetails);
        mDataModel = new BuildListDataModelImpl(buildList);
    }

    @Test
    public void testGetBuildNumberIfItIsNull() throws Exception {
        when(mBuildDetails.getNumber()).thenReturn(null);
        assertThat(mDataModel.getBuildNumber(0), is(""));
    }

    @Test
    public void testGetBuildNumberIfItIsNotNull() throws Exception {
        when(mBuildDetails.getNumber()).thenReturn("123");
        assertThat(mDataModel.getBuildNumber(0), is("#123"));
    }

    @Test
    public void testGetBranchName() throws Exception {
        when(mBuildDetails.getBranchName()).thenReturn("branch");
        assertThat(mDataModel.getBranchName(0), is("branch"));
    }

    @Test
    public void testGetBuildStatusIcon() throws Exception {
        when(mBuildDetails.getStatusIcon()).thenReturn("icon");
        assertThat(mDataModel.getBuildStatusIcon(0), is("icon"));
    }

    @Test
    public void testGetStatusText() throws Exception {
        when(mBuildDetails.getStatusText()).thenReturn("text");
        assertThat(mDataModel.getStatusText(0), is("text"));
    }

    @Test
    public void testGetBuild() throws Exception {
        when(mBuildDetails.toBuild()).thenReturn(mBuild);
        assertThat(mDataModel.getBuild(0), is(mBuild));
    }

    @Test
    public void testAddDataModel() throws Exception {
        BuildDetails build = new BuildDetailsImpl(mBuild);
        BuildListDataModel dataModel = new BuildListDataModelImpl(Collections.singletonList(build));
        mDataModel.addMoreBuilds(dataModel);
        assertThat(mDataModel.getBuild(1), is(mBuild));
        assertThat(mDataModel.getItemCount(), is(2));
    }

    @Test
    public void testGetItemCount() throws Exception {
        assertThat(mDataModel.getItemCount(), is(1));
    }

    @Test
    public void testGetStartDate() throws Exception {
        when(mBuildDetails.getStartDateFormattedAsHeader()).thenReturn("date");
        assertThat(mDataModel.getStartDate(0), is("date"));
    }

    @Test
    public void testGetBuildTypeId() throws Exception {
        when(mBuildDetails.getBuildTypeId()).thenReturn("id");
        assertThat(mDataModel.getBuildTypeId(0), is("id"));
    }

    @Test
    public void testHasBuildTypeInfo() throws Exception {
        when(mBuildDetails.hasBuildTypeInfo()).thenReturn(false);
        assertThat(mDataModel.hasBuildTypeInfo(0), is(false));
    }

    @Test
    public void testGetBuildTypeName() throws Exception {
        when(mBuildDetails.getBuildTypeFullName()).thenReturn("name");
        assertThat(mDataModel.getBuildTypeFullName(0), is("name"));
    }

    @Test
    public void testHasBranch() {
        when(mBuildDetails.getBranchName()).thenReturn("");
        assertThat(mDataModel.hasBranch(0), is(true));
    }

    @Test
    public void testIsPersonal() {
        when(mBuildDetails.isPersonal()).thenReturn(true);
        assertThat(mDataModel.isPersonal(0), is(true));
    }

    @Test
    public void testIsPinned() {
        when(mBuildDetails.isPinned()).thenReturn(true);
        assertThat(mDataModel.isPinned(0), is(true));
    }

    @Test
    public void testIsQueued() {
        when(mBuildDetails.isQueued()).thenReturn(true);
        assertThat(mDataModel.isQueued(0), is(true));
    }

    @Test
    public void testIsLoadMore() {
        mDataModel = new BuildListDataModelImpl(Collections.singletonList(BuildListDataModelImpl.Companion.getLOAD_MORE()));
        assertThat(mDataModel.isLoadMore(0), is(true));
    }

    @Test
    public void testLoadMore() {
        mDataModel.addLoadMore();
        assertThat(mDataModel.isLoadMore(1), is(true));
        assertThat(mDataModel.getItemCount(), is(2));
        mDataModel.removeLoadMore();
        assertThat(mDataModel.isLoadMore(0), is(false));
        assertThat(mDataModel.getItemCount(), is(1));
    }

}