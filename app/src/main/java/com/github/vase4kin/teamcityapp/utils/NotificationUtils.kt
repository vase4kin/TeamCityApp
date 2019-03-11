package com.github.vase4kin.teamcityapp.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import com.github.vase4kin.teamcityapp.R

const val DEFAULT_NOTIFICATIONS_CHANNEL_ID = "default_notifications"

@TargetApi(Build.VERSION_CODES.O)
private fun createAppNotificationChannels(context: Context): List<NotificationChannel> = listOf(
        NotificationChannel(
                DEFAULT_NOTIFICATIONS_CHANNEL_ID,
                context.getString(R.string.default_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT
        )
)

private fun createNotificationChannel(context: Context, channels: List<NotificationChannel>) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    channels.forEach { channel ->
        notificationManager.createNotificationChannel(channel)
    }
}

fun initAppNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    createNotificationChannel(context, createAppNotificationChannels(context))
}