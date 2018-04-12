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

package com.github.vase4kin.teamcityapp.artifact.api;

import com.github.vase4kin.teamcityapp.base.api.BaseObject;

/**
 * Artifact file
 */
public class File extends BaseObject {

    private String name;
    private String modificationTime;
    private long size;
    private Children children;
    private Content content;

    @Override
    public String getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getModificationTime() {
        return modificationTime;
    }

    public long getSize() {
        return size;
    }

    public Children getChildren() {
        return children;
    }

    public Content getContent() {
        return content;
    }

    public boolean hasChildren() {
        return children != null;
    }

    public boolean isFolder() {
        return size == 0;
    }

    /**
     * Children
     */
    public static class Children extends BaseObject {

        public Children(String href) {
            this.href = href;
        }
    }

    /**
     * Content
     */
    public static class Content extends BaseObject {

        public Content(String href) {
            this.href = href;
        }
    }

    //folder
    public File(String name, Children children, String href) {
        this.name = name;
        this.children = children;
        this.href = href;
    }

    // file
    public File(String name, long size, Content content, String href) {
        this.name = name;
        this.size = size;
        this.content = content;
        this.href = href;
    }
}
