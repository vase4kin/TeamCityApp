package com.github.vase4kin.teamcityapp.utils

import android.app.Activity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun createProgressDialog(
    activity: Activity,
    @StringRes message: Int,
    cancelable: Boolean = false
): AlertDialog {
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_progress, null)
    view.findViewById<TextView>(R.id.progress_dialog_message).setText(message)
    return MaterialAlertDialogBuilder(activity)
        .setView(view)
        .setCancelable(cancelable)
        .create()
        .apply { setCanceledOnTouchOutside(false) }
}
