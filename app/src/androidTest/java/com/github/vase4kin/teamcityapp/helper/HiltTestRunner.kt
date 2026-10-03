package com.github.vase4kin.teamcityapp.helper

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import dagger.hilt.android.testing.CustomTestApplication

@CustomTestApplication(TeamCityApplicationBase::class)
interface TeamCityTestApplication

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, TeamCityTestApplication_Application::class.java.name, context)
}
