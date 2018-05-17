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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BottomSheetDataModelImplTest {

    private static final String URL = "/app/rest/builds/id:993171/artifacts/content/stdlib-docs.zip";
    private static final String FILE_NAME = "stdlib-docs.zip";

    @Mock
    private BottomSheetItem item;

    private BottomSheetDataModelImpl dataModel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(item.getTitle()).thenReturn("title");
        dataModel = new BottomSheetDataModelImpl(Collections.singletonList(item));
    }

    @Test
    public void testGetFileName() {
        when(item.getDescription()).thenReturn(URL);
        assertThat(dataModel.getFileName(0), is(FILE_NAME));
    }

    @Test
    public void testGetFileNameIfUrlIsNotCorrect() {
        String url = "url";
        when(item.getDescription()).thenReturn(url);
        assertThat(dataModel.getFileName(0), is(url));
    }
}