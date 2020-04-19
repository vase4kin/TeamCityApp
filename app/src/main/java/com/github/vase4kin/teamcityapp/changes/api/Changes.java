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

import androidx.annotation.NonNull;

import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;
import com.github.vase4kin.teamcityapp.utils.DateUtils;

import java.util.List;

import teamcityapp.libraries.api.BaseObject;

/**
 * Changes
 */
public class Changes extends BaseObject implements Collectible<Changes.Change> {

    private List<Change> change;
    private int count;
    private String nextHref;

    @Override
    public List<Change> getObjects() {
        return change;
    }

    public int getCount() {
        return count;
    }

    public void setChanges(List<Change> change) {
        this.change = change;
    }

    public String getNextHref() {
        return nextHref;
    }

    /**
     * Change
     */
    public static class Change extends BaseObject {

        private String version;
        private String username;
        private String date;
        private String webUrl;
        private String comment;
        private ChangeFiles files;

        public ChangeFiles getFiles() {
            return files;
        }

        public String getVersion() {
            return version;
        }

        public String getUsername() {
            return username;
        }

        public String getDate() {
            return DateUtils.initWithDate(date).formatStartDateToBuildTitle();
        }

        @NonNull
        public String getWebUrl() {
            return webUrl;
        }

        public String getComment() {
            return comment;
        }

        // default
        public Change() {
        }

        public Change(String href) {
            this.href = href;
        }

        public Change(String version, String username, String date, String comment, ChangeFiles files, String webUrl, String id) {
            this.version = version;
            this.username = username;
            this.date = date;
            this.comment = comment;
            this.files = files;
            this.webUrl = webUrl;
            this.id = id;
        }
    }

    public Changes(String href) {
        this.href = href;
    }

    public Changes(List<Change> change, int count) {
        this.change = change;
        this.count = count;
    }
}
