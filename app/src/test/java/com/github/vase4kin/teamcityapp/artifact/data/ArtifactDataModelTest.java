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

package com.github.vase4kin.teamcityapp.artifact.data;

import com.github.vase4kin.teamcityapp.artifact.api.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ArtifactDataModelTest {

    @Mock
    private File mFile;

    private ArtifactDataModelImpl mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mFile.getSize()).thenReturn(1L);
        List<File> files = new ArrayList<>();
        files.add(mFile);
        mDataModel = new ArtifactDataModelImpl(files);
    }

    @Test
    public void testGetSize() throws Exception {
        assertThat(mDataModel.getSize(0), is(1L));
    }

    @Test
    public void testGetFile() throws Exception {
        assertThat(mDataModel.getFile(0), is(mFile));
    }

    @Test
    public void testItemCount() throws Exception {
        assertThat(mDataModel.getItemCount(), is(1));
    }
}