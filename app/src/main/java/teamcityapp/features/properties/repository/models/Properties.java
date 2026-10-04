/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.properties.repository.models;

import java.util.List;

import teamcityapp.libraries.api.BaseObject;

/**
 * Properties
 */
public class Properties extends BaseObject {

    private List<Property> property;

    public List<Property> getProperties() {
        return property;
    }

    public static class Property extends BaseObject {

        private String name;
        private String value;
        private boolean own;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isOwn() {
            return own;
        }

        public Property(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public Properties() {
    }

    public Properties(List<Property> property) {
        this.property = property;
    }
}
