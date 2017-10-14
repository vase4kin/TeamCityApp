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

package com.github.vase4kin.teamcityapp.account.create.helper;

import android.support.annotation.NonNull;

/**
 * Url formatter
 */
public interface UrlFormatter {

    /**
     * Format server url
     *
     * @param serverUrl - Server url to format
     * @return formatted server url
     */
    String formatServerUrl(String serverUrl);

    /**
     * Remove leading slash from url to able load data if server url contains trailing path, teamcity.com/server
     * <p>
     * https://github.com/square/retrofit/issues/907
     * <p>
     * ¯\_(ツ)_/¯
     * <p>
     * /app/rest/buildTypes/id:buildType/builds?locator=locator:any - > app/rest/buildTypes/id:buildType/builds?locator=locator:any
     */
    String formatBasicUrl(@NonNull String url);
}

