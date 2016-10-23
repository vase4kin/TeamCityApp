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

package com.github.vase4kin.teamcityapp.storage;

import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

/**
 * Users factory
 */
public class UsersFactory {

    private static final String GUEST_USER_USER_NAME = "Guest user";
    private static final String EMPTY_STRING = "";

    /**
     * @return Guest user
     */
    static UserAccount guestUser(String serverUrl) {
        return new UserAccount(serverUrl, GUEST_USER_USER_NAME, EMPTY_STRING, true, true);
    }

    /**
     * @return Registered user
     */
    static UserAccount user(String serverUrl, String userName, String password) {
        return new UserAccount(serverUrl, userName, password, false, true);
    }

    /**
     * @return Empty user
     */
    static UserAccount emptyUser() {
        return new UserAccount(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, true, true);
    }
}
