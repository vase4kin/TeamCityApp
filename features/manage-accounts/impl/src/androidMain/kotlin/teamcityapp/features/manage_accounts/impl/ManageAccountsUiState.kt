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

package teamcityapp.features.manage_accounts.impl

import teamcityapp.libraries.resources.R as SharedR

val ManageAccountsDialog.titleRes: Int? get() = if (this == ManageAccountsDialog.SslWarning) SharedR.string.warning_ssl_dialog_title else null
val ManageAccountsDialog.messageRes: Int? get() = when (this) {
    ManageAccountsDialog.SslWarning -> SharedR.string.warning_ssl_dialog_content
    is ManageAccountsDialog.ConfirmRemoval -> R.string.dialog_remove_not_active_account_positive_content_text
    else -> null
}
val ManageAccountsDialog.confirmLabelRes: Int? get() = when (this) {
    ManageAccountsDialog.SslWarning -> android.R.string.ok
    is ManageAccountsDialog.ConfirmRemoval -> R.string.dialog_remove_active_account_positive_button_text
    else -> null
}
