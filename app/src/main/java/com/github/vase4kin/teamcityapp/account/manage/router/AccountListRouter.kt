package com.github.vase4kin.teamcityapp.account.manage.router

import android.app.Activity
import android.content.Intent
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.view.LoginActivity

interface AccountListRouter {

    fun openHome()

    fun openCreateNewAccount()

    fun openLogin()
}

class AccountListRouterImpl(private val activity: Activity) : AccountListRouter {

    override fun openHome() {
        HomeActivity.startWhenSwitchingAccountsFromDrawer(activity)
    }

    override fun openCreateNewAccount() {
        activity.startActivity(Intent(activity, CreateAccountActivity::class.java))
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
    }

    override fun openLogin() {
        LoginActivity.startWithClearStack(activity)
    }

}