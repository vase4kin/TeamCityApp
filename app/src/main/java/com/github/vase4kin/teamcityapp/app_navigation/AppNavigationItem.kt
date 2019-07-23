package com.github.vase4kin.teamcityapp.app_navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.vase4kin.teamcityapp.R

enum class AppNavigationItem(@DrawableRes val icon: Int, @StringRes val title: Int) {
    HOME(R.drawable.ic_home_white_24dp, R.string.projects_drawer_item),
    FAVORITES(R.drawable.ic_favorite_white_24dp, R.string.favorites_drawer_item),
    RUNNING_BUILDS(R.drawable.ic_directions_run_white_24px, R.string.running_builds_drawer_item),
    BUILD_QUEUE(R.drawable.ic_layers_white_24dp, R.string.build_queue_drawer_item)
}