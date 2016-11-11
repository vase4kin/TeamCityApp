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

package com.github.vase4kin.teamcityapp.changes.data;

import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.changes.view.ChangesAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ChangesDataModelImplTest {

    @Mock
    private List<String> mFiles;

    @Mock
    private ChangeFiles mChangeFiles;

    @Mock
    private Changes.Change mChange;

    private ChangesDataModel mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<Changes.Change> changes = new ArrayList<>();
        changes.add(mChange);
        mDataModel = new ChangesDataModelImpl(changes);
    }

    @Test
    public void testGetVersion() throws Exception {
        when(mChange.getVersion()).thenReturn("version");
        assertThat(mDataModel.getVersion(0), is("version"));
    }

    @Test
    public void testGetUserName() throws Exception {
        when(mChange.getUsername()).thenReturn("username");
        assertThat(mDataModel.getUserName(0), is("username"));
    }

    @Test
    public void testGetDate() throws Exception {
        when(mChange.getDate()).thenReturn("date");
        assertThat(mDataModel.getDate(0), is("date"));
    }

    @Test
    public void testGetItemCount() throws Exception {
        assertThat(mDataModel.getItemCount(), is(1));
    }

    @Test
    public void testGetComment() throws Exception {
        when(mChange.getComment()).thenReturn("comment");
        assertThat(mDataModel.getComment(0), is("comment"));
    }

    @Test
    public void testGetFilesCountIfFilesAreNull() throws Exception {
        when(mChange.getFiles()).thenReturn(null);
        assertThat(mDataModel.getFilesCount(0), is(0));
    }

    @Test
    public void testGetFilesCountIfFilesAreNotNull() throws Exception {
        when(mFiles.size()).thenReturn(34);
        when(mChangeFiles.getFiles()).thenReturn(mFiles);
        when(mChange.getFiles()).thenReturn(mChangeFiles);
        assertThat(mDataModel.getFilesCount(0), is(34));
    }

    @Test
    public void testGetChange() throws Exception {
        assertThat(mDataModel.getChange(0), is(mChange));
    }

    @Test
    public void testIsLoadMore() throws Exception {
        List<Changes.Change> changes = new ArrayList<>();
        changes.add(new ChangesAdapter.LoadMore());
        mDataModel = new ChangesDataModelImpl(changes);
        assertThat(mDataModel.isLoadMore(0), is(true));
    }

    @Test
    public void testAdd() throws Exception {
        Changes.Change change = new Changes.Change();
        mDataModel.addLoadMore(change);
        assertThat(mDataModel.getChange(1), is(change));
    }

    @Test
    public void testRemove() throws Exception {
        mDataModel.removeLoadMore(mChange);
        assertThat(mDataModel.getItemCount(), is(0));
    }

    @Test
    public void testAddDataModel() throws Exception {
        List<Changes.Change> changes = new ArrayList<>();
        ChangesAdapter.LoadMore loadMore = new ChangesAdapter.LoadMore();
        changes.add(loadMore);
        ChangesDataModel dataModel = new ChangesDataModelImpl(changes);
        mDataModel.add(dataModel);
        assertThat(mDataModel.getItemCount(), is(2));
        assertThat(mDataModel.isLoadMore(1), is(true));
    }
}