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

package teamcityapp.libraries.time
import platform.Foundation.*
private fun formatter(pattern: String) = NSDateFormatter().apply {
    locale = NSLocale("en_US_POSIX")
    dateFormat = pattern
    lenient = false
}
actual fun parseTeamCityLocalDate(raw: String): Long? {
    if (raw.length < 15) return null
    return formatter("yyyyMMdd'T'HHmmss").dateFromString(raw.take(15))?.let { (it.timeIntervalSince1970 * 1000).toLong() }
}
actual fun formatTeamCityLocalDate(epochMilliseconds: Long, pattern: String): String = formatter(pattern).stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMilliseconds / 1000.0))
actual fun currentLocalYear(): Int = formatter("yyyy").stringFromDate(NSDate()).toInt()
