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

package teamcityapp.features.change.view

import teamcityapp.features.change.router.ChangeRouter

interface ChangeItemsFactory {
    fun createChangeDetailsCard(
        comment: String,
        version: String,
        userName: String,
        date: String,
        webUrl: String
    ): ChangeDetailsItem

    fun createChangeFilesHeader(fileSize: Int): ChangeFileItemsHeader
    fun createChangeFile(
        id: String,
        changeFile: ChangeActivity.ChangeFile
    ): ChangeFileItem
}

class ChangeItemsFactoryImpl(
    private val router: ChangeRouter
) : ChangeItemsFactory {

    override fun createChangeDetailsCard(
        comment: String,
        version: String,
        userName: String,
        date: String,
        webUrl: String
    ): ChangeDetailsItem {
        return ChangeDetailsItem(
            comment = comment.trim(),
            version = version,
            userName = userName,
            webUrl = webUrl,
            date = date,
            router = router
        )
    }

    override fun createChangeFilesHeader(fileSize: Int): ChangeFileItemsHeader {
        return ChangeFileItemsHeader(fileSize)
    }

    override fun createChangeFile(
        id: String,
        changeFile: ChangeActivity.ChangeFile
    ): ChangeFileItem {
        return ChangeFileItem(
            id = id, changeFile = changeFile, router = router
        )
    }
}