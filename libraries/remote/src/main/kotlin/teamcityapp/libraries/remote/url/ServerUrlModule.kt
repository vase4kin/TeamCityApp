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

package teamcityapp.libraries.remote.url
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
/** Hilt binding; the included module also supports the existing account API graph. */
@Module(includes = [UrlFormatterModule::class])
@InstallIn(SingletonComponent::class)
object ServerUrlModule
