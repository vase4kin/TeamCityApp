package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter

enum class Filter(val code: Int) {
    RUNNING_ALL(0), RUNNING_FAVORITES(1), QUEUE_ALL(2), QUEUE_FAVORITES(3);

    val isRunning: Boolean
        get() = this == RUNNING_ALL || this == RUNNING_FAVORITES

    val isQueued: Boolean
        get() = this == QUEUE_ALL || this == QUEUE_FAVORITES

    fun opposite(): Filter {
        return when (this) {
            RUNNING_ALL -> RUNNING_FAVORITES
            RUNNING_FAVORITES -> RUNNING_ALL
            QUEUE_ALL -> QUEUE_FAVORITES
            QUEUE_FAVORITES -> QUEUE_ALL
        }
    }
}