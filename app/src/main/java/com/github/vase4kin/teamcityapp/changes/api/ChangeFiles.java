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

package com.github.vase4kin.teamcityapp.changes.api;

import java.util.ArrayList;
import java.util.List;

import teamcityapp.libraries.api.BaseObject;

/**
 * Changes
 */
public class ChangeFiles extends BaseObject {

    private List<ChangeFile> file;

    public List<String> getFiles() {
        List<String> files = new ArrayList<>();
        for (ChangeFile changeFile : file) {
            files.add(changeFile.getFile());
        }
        return files;
    }

    public List<ChangeFile> getFile() {
        return file;
    }

    public static class ChangeFile extends BaseObject {

        private String file;
        private String changeType;

        public String getFile() {
            return file;
        }

        public String getChangeType() {
            return changeType;
        }

        public ChangeFile(String file, String type) {
            this.file = file;
            this.changeType = type;
        }
    }

    public ChangeFiles(List<ChangeFile> file) {
        this.file = file;
    }
}
