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

package teamcity.features.test_details.api.models;

import teamcityapp.libraries.api.BaseObject;

public class TestOccurrence extends BaseObject {

    private static final String STATUS_FAILURE = "FAILURE";

    private String name;
    private String status;
    private int duration;
    private String details;

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getDuration() {
        return duration;
    }

    public String getDetails() {
        return details;
    }

    public boolean isFailed() {
        return STATUS_FAILURE.equals(status);
    }

    //default
    public TestOccurrence() {
    }

    public TestOccurrence(String details) {
        this.details = details;
    }

    public TestOccurrence(String name, String status, String href) {
        this.name = name;
        this.status = status;
        this.href = href;
    }
}
