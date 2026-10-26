/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.change_details.impl

import android.os.Bundle
import teamcityapp.features.change_details.api.ChangeDetails

internal object ChangeDetailsArguments {
    const val ID = ChangeDetailsKeys.ID
    const val COMMENT = ChangeDetailsKeys.COMMENT
    const val USER = ChangeDetailsKeys.USER
    const val DATE = ChangeDetailsKeys.DATE
    const val FILE_NAMES = ChangeDetailsKeys.FILE_NAMES
    const val FILE_TYPES = ChangeDetailsKeys.FILE_TYPES
    const val REVISION = ChangeDetailsKeys.REVISION
    const val WEB_URL = ChangeDetailsKeys.WEB_URL

    fun bundle(details: ChangeDetails) = Bundle().apply {
        putString(ID, details.id)
        putString(COMMENT, details.comment)
        putString(USER, details.userName)
        putString(DATE, details.date)
        putStringArrayList(FILE_NAMES, ArrayList(details.files.map { it.name }))
        putStringArrayList(FILE_TYPES, ArrayList(details.files.map { it.type }))
        putString(REVISION, details.revision)
        putString(WEB_URL, details.webUrl)
    }
}
