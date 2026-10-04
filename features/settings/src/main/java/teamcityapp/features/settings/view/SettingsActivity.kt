package teamcityapp.features.settings.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.features.settings.viewmodel.SettingsViewModel
import teamcityapp.libraries.theme.TeamCityTheme
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    @Inject lateinit var tracker: SettingsTracker
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            TeamCityTheme {
                SettingsScreen(state, viewModel.availableThemes, viewModel::selectTheme, viewModel::retry, ::finish)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tracker.trackView()
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, SettingsActivity::class.java))
        }
    }
}
