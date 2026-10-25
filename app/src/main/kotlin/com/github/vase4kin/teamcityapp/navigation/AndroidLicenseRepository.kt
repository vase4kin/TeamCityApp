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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Context
import com.github.vase4kin.teamcityapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import teamcityapp.shared.AppLicense
import teamcityapp.shared.AppLicenseRepository

/** Reads the OSS plugin's actual packaged Android inventory; the reader UI is common. */
internal class AndroidLicenseRepository(context: Context) : AppLicenseRepository {
    private val resources = context.applicationContext.resources
    private data class Entry(val license: AppLicense, val offset: Int, val count: Int)
    private fun inventory(): List<Entry> = resources.openRawResource(R.raw.third_party_license_metadata).bufferedReader().useLines { lines ->
        lines.filter { it.isNotBlank() }.mapIndexed { index, line ->
            val separator = line.indexOf(' ')
            require(separator > 0)
            val (offset, count) = line.substring(0, separator).split(':').map(String::toInt)
            require(offset >= 0 && count >= 0)
            Entry(AppLicense("android_$index", line.substring(separator + 1)), offset, count)
        }.toList()
    }
    override suspend fun entries(): List<AppLicense> = withContext(Dispatchers.IO) { inventory().map { it.license } }
    override suspend fun text(id: String): String = withContext(Dispatchers.IO) {
        val entry = inventory().single { it.license.id == id }
        val bytes = resources.openRawResource(R.raw.third_party_licenses).use { it.readBytes() }
        require(entry.offset <= bytes.size && entry.count <= bytes.size - entry.offset)
        bytes.copyOfRange(entry.offset, entry.offset + entry.count).decodeToString()
    }
}
