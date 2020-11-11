/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import teamcityapp.features.about.repository.AboutRepository
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.libraries.utils.initToolbar
import javax.inject.Inject

/**
 * About activity
 */
class AboutActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var repository: AboutRepository

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initToolbar()
        loadServerInfo()
    }

    private fun loadServerInfo() {
        repository.serverInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                showLoader()
            }
            .doFinally {
                hideLoader()
            }
            .subscribeBy(
                onError = {
                    showAboutFragment()
                },
                onSuccess = {
                    showAboutFragment(it)
                }
            )
            .addTo(subscriptions)
    }

    private fun showAboutFragment(serverInfo: ServerInfo? = null) {
        // Commit fragment to container
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.about_library_container, AboutFragment.create(serverInfo))
            .commit()
    }

    private fun showLoader() {
        findViewById<View>(R.id.progress_wheel).visibility = View.VISIBLE
    }

    private fun hideLoader() {
        findViewById<View>(R.id.progress_wheel).visibility = View.GONE
    }

    companion object {

        /**
         * Start About activity with Flag [Intent.FLAG_ACTIVITY_SINGLE_TOP]
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, AboutActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(launchIntent)
        }
    }
}
