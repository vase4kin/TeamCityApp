package com.github.vase4kin.teamcityapp.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.drawer.dagger.DrawerModule
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var drawerPresenter: DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Injecting presenter
        DaggerSettingsComponent.builder()
                .drawerModule(DrawerModule(this, false, DrawerView.SETTINGS))
                .restApiComponent((application as TeamCityApplication).restApiInjector)
                .build()
                .inject(this)

        drawerPresenter.onCreate()

        addFragment()
    }

    override fun onBackPressed() {
        drawerPresenter.onBackButtonPressed()
    }

    private fun addFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.settings, SettingsFragment())
                .addToBackStack(null)
                .commit()
    }

    internal class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    companion object {
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, SettingsActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            DrawerActivityStartUtils.startActivity(launchIntent, activity)
        }
    }
}