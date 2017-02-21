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

package com.github.vase4kin.teamcityapp.tests.data;

import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestsDataModelImplTest {

    @Mock
    private TestOccurrences.TestOccurrence mTest;

    private TestsDataModelImpl mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<TestOccurrences.TestOccurrence> tests = new ArrayList<>();
        tests.add(mTest);
        mDataModel = new TestsDataModelImpl(tests);
    }

    @Test
    public void testIsFailed() throws Exception {
        when(mTest.isFailed()).thenReturn(true);
        assertThat(mDataModel.isFailed(0), is(equalTo(true)));
    }

    @Test
    public void testGetName() throws Exception {
        when(mTest.getName()).thenReturn("name");
        assertThat(mDataModel.getName(0), is(equalTo("name")));
    }

    @Test
    public void testGetStatusIcon() throws Exception {
        when(mTest.getStatus()).thenReturn("FAILURE");
        assertThat(mDataModel.getStatusIcon(0), is(equalTo(IconUtils.ICON_FAILURE)));
    }

    @Test
    public void testGetHref() throws Exception {
        when(mTest.getHref()).thenReturn("href");
        assertThat(mDataModel.getHref(0), is(equalTo("href")));
    }

    @Test
    public void testGetStatus() throws Exception {
        when(mTest.getStatus()).thenReturn("status");
        assertThat(mDataModel.getStatus(0), is(equalTo("status")));
    }

    @Test
    public void testGetItemCount() throws Exception {
        assertThat(mDataModel.getItemCount(), is(equalTo(1)));
    }

    @Test
    public void testAddTestDataModel() throws Exception {
        TestOccurrences.TestOccurrence testOccurrence = new TestOccurrences.TestOccurrence();
        mDataModel.addMoreBuilds(new TestsDataModelImpl(Collections.singletonList(testOccurrence)));
        assertThat(mDataModel.getItemCount(), is(equalTo(2)));
    }

    @Test
    public void testIsLoadMore() {
        mDataModel = new TestsDataModelImpl(Collections.singletonList(TestsDataModelImpl.LOAD_MORE));
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