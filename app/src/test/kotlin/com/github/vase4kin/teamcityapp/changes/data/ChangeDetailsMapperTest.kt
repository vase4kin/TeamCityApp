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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles
import com.github.vase4kin.teamcityapp.changes.api.Changes
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile

class ChangeDetailsMapperTest {
    private fun change(): Changes.Change = mock(Changes.Change::class.java).apply {
        `when`(getId()).thenReturn("123")
        `when`(comment).thenReturn(" raw comment ")
        `when`(username).thenReturn("Developer")
        `when`(date).thenReturn("01 Oct 2026")
        `when`(version).thenReturn("abc123")
        `when`(webUrl).thenReturn("https://teamcity.example/change/123")
    }

    @Test fun mapsFormattedDateAndPreservesFieldValuesAndDuplicateFileOrder() {
        val change = change()
        `when`(change.files).thenReturn(ChangeFiles(listOf(ChangeFiles.ChangeFile("same.kt", "added"), ChangeFiles.ChangeFile("same.kt", "removed"))))
        assertEquals(
            ChangeDetails(
                "123",
                " raw comment ",
                "Developer",
                "01 Oct 2026",
                listOf(ChangedFile("same.kt", "added"), ChangedFile("same.kt", "removed")),
                "abc123",
                "https://teamcity.example/change/123"
            ),
            change.toChangeDetails()
        )
    }

    @Test fun absentFilesBecomeAnEmptyList() {
        assertTrue(change().toChangeDetails().files.isEmpty())
    }

    @Test fun mappingCopiesMutableDtoFileList() {
        val change = change()
        val files = mutableListOf(ChangeFiles.ChangeFile("a.kt", "added"))
        `when`(change.files).thenReturn(ChangeFiles(files))
        val result = change.toChangeDetails()
        files.clear()
        assertEquals(listOf(ChangedFile("a.kt", "added")), result.files)
    }
}
