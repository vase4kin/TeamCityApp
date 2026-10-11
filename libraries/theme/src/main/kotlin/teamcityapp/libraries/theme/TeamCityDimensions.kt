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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shared dimensions for Compose screens and components. Keep literal dp values here;
 * text sizes belong to MaterialTheme.typography and measured sizes stay dynamic.
 */
object TeamCityDimensions {
    // Spacing and padding.
    val noSpacing = 0.dp
    val extraSmallSpacing = 4.dp
    val compactSpacing = 6.dp
    val smallSpacing = 8.dp
    val mediumSpacing = 12.dp
    val contentPadding = 16.dp
    val largeContentPadding = 20.dp
    val sectionSpacing = 24.dp
    val extraLargeSpacing = 32.dp

    // Controls, icons, and feedback.
    val minimumTouchTarget = 48.dp
    val controlMinHeight = 56.dp
    val leadingContentWidth = 56.dp
    val smallIconSize = 16.dp
    val statusIconSize = 18.dp
    val compactIconSize = 20.dp
    val iconSize = 24.dp
    val largeIconSize = 32.dp
    val messageIconSize = 64.dp
    val emptyStateIconSize = 124.dp
    val compactProgressIndicatorSize = 20.dp
    val progressIndicatorSize = 32.dp
    val modalProgressIndicatorSize = 48.dp
    val progressStrokeWidth = 2.dp
    val lowElevation = 1.dp
    val loginLogoSize = 96.dp
    val splashLogoSize = 160.dp
    val floatingActionLaneHeight = 112.dp

    // Width limits and responsive layout thresholds.
    val messageMaxWidth = 480.dp
    val formMaxWidth = 560.dp
    val paneMaxWidth = 640.dp
    val screenContentMaxWidth = 720.dp
    val aboutContentMaxWidth = 840.dp
    val errorNoticeInlineMinWidth = 324.dp
    val historyInlineActionMinWidth = 480.dp
    val retryActionMaxWidth = 200.dp
    val parameterListMaxHeight = 360.dp

    // Shapes and list geometry.
    val extraSmallCornerRadius = 8.dp
    val smallCornerRadius = 12.dp
    val mediumCornerRadius = 20.dp
    val largeCornerRadius = 28.dp
    val extraLargeCornerRadius = 32.dp
    val listRowMinHeight = 72.dp
    val selectionRowMinHeight = 64.dp
    val listRowVerticalPadding = 14.dp
    val listRowVerticalSpacing = 1.dp
    val drawerRowSpacing = 2.dp
    val listPressedCornerRadius = 16.dp
    val listOuterCornerRadius = 24.dp
    val listInnerCornerRadius = 4.dp
    val listLeadingContainerSize = 40.dp
    val listLeadingCornerRadius = 14.dp
    val skeletonTitleHeight = 16.dp
    val skeletonBodyHeight = 12.dp
    val propertyRowSpacing = 3.dp
    val propertyRowVerticalPadding = 18.dp
    val propertyContentSpacing = 14.dp

    // Sheet headers, error illustrations, and coachmarks.
    val sheetHandleWidth = 40.dp
    val sheetHandleHeight = 4.dp
    val sheetHandleCornerRadius = 2.dp
    val sheetHeaderMinHeight = 76.dp
    val errorIllustrationSize = 88.dp
    val errorIllustrationIconSize = 36.dp
    val errorIllustrationCornerRadius = 44.dp
    val coachmarkCornerRadius = 16.dp
    val coachmarkStrokeWidth = 2.dp
}

internal val TeamCityShapes = Shapes(
    extraSmall = RoundedCornerShape(TeamCityDimensions.extraSmallCornerRadius),
    small = RoundedCornerShape(TeamCityDimensions.smallCornerRadius),
    medium = RoundedCornerShape(TeamCityDimensions.mediumCornerRadius),
    large = RoundedCornerShape(TeamCityDimensions.largeCornerRadius),
    extraLarge = RoundedCornerShape(TeamCityDimensions.extraLargeCornerRadius)
)
