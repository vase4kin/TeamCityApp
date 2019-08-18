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

package com.github.vase4kin.teamcityapp.properties.data;

import com.github.vase4kin.teamcityapp.properties.api.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class PropertiesDataModelImplTest {

    @Mock
    private Properties.Property mProperty;

    private PropertiesDataModel mDataModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<Properties.Property> properties = new ArrayList<>();
        properties.add(mProperty);
        mDataModel = new PropertiesDataModelImpl(properties);
    }

    @Test
    public void testGetName() {
        when(mProperty.getName()).thenReturn("name");
        assertThat(mDataModel.getName(0), is(equalTo("name")));
    }

    @Test
    public void testGetValueIfEmpty() {
        when(mProperty.getValue()).thenReturn("");
        assertThat(mDataModel.getValue(0), is(equalTo(PropertiesDataModelImpl.Companion.getEMPTY())));
    }

    @Test
    public void testGetValueIfNotEmpty() {
        when(mProperty.getValue()).thenReturn("value");
        assertThat(mDataModel.getValue(0), is(equalTo("value")));
    }

    @Test
    public void testIsEmpty() {
        when(mProperty.getValue()).thenReturn(PropertiesDataModelImpl.Companion.getEMPTY());
        assertThat(mDataModel.isEmpty(0), is(equalTo(true)));
    }

    @Test
    public void testGetItemCount() {
        assertThat(mDataModel.getItemCount(), is(1));
    }
}