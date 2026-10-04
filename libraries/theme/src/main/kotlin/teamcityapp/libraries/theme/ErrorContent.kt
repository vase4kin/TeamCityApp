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

package teamcityapp.libraries.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import teamcityapp.libraries.resources.R as SharedR

/** Compose replacement for the legacy error widget, retaining its visual resource. */
@Composable
fun ErrorContent(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primary = if (dark) Color.White.copy(alpha = 0.87f) else Color.Black.copy(alpha = 0.87f)
    val secondary = if (dark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val painter = painterResource(tr.xip.errorview.R.drawable.error_view_cloud)
            Icon(painter, null, tint = if (dark) Color.White else secondary)
            Text(stringResource(SharedR.string.error_view_oops_message), Modifier.padding(top = 16.dp),
                color = primary, style = TextStyle(fontSize = 18.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)))
            Text(stringResource(SharedR.string.error_view_error_text), Modifier.padding(top = 8.dp),
                color = secondary, textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)))
            Box(Modifier.padding(top = 16.dp).clickable(role = Role.Button, onClick = onRetry)
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(SharedR.string.error_view_retry_button_text), Modifier.padding(8.dp),
                    color = secondary, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = true)))
            }
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() { TeamCityTheme { ErrorContent(Modifier.fillMaxSize(), {}) } }
