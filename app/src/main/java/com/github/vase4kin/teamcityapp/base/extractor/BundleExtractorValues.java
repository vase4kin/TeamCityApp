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

package com.github.vase4kin.teamcityapp.base.extractor;

/**
 * Default bundle passing values
 */
public interface BundleExtractorValues {

    String BUILD = "build";
    String URL = "url";
    String NAME = "name";
    String ID = "id";
    String AGENT_TYPE = "agent.type";
    String IS_REQUIRED_TO_RELOAD = "isRequiredToReload";
    String IS_NEW_ACCOUNT_CREATED = "isNewAccountCreated";
    String PASSED_COUNT_PARAM = "passedCount";
    String FAILED_COUNT_PARAM = "failedCount";
    String IGNORED_COUNT_PARAM = "ignoredCount";
    String TEST_URL = "testUrl";
    String BUILD_ID = "buildId";
}
