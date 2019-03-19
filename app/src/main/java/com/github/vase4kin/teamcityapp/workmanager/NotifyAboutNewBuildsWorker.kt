package com.github.vase4kin.teamcityapp.workmanager

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.utils.DEFAULT_NOTIFICATIONS_CHANNEL_ID
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject


class NotifyAboutNewBuildsWorker(
        appContext: Context, workerParams: WorkerParameters
) : RxWorker(appContext, workerParams) {

    @Inject
    internal lateinit var sharedUserStorage: SharedUserStorage

    @Inject
    internal lateinit var repository: Repository

    override fun createWork(): Single<ListenableWorker.Result>? {
        (applicationContext as TeamCityApplication).restApiInjector.inject(this)
        val favoriteBuildTypes = sharedUserStorage.activeUser.favoriteBuildTypes
        return if (favoriteBuildTypes.isEmpty()) {
            Single.just<Result>(ListenableWorker.Result.success())
        } else {
            val favoriteBuildTypeIds = favoriteBuildTypes.keys
            Observable.fromIterable(favoriteBuildTypeIds)
                    .flatMapSingle { buildTypeId ->
                        repository.listBuilds(buildTypeId, BuildListFilter.DEFAULT_FILTER_LOCATOR, true)
                    }
                    .flatMap { Observable.fromIterable(it.objects) }
                    .flatMapSingle { serverBuild ->
                        // Make sure cache is updated
                        val serverBuildDetails = BuildDetailsImpl(serverBuild)
                        // If server build's running update cache immediately
                        if (serverBuildDetails.isRunning) {
                            repository.build(serverBuild.href, true)
                        } else {
                            // Call cache
                            repository.build(serverBuild.href, false)
                                    .flatMap { cachedBuild ->
                                        val cacheBuildDetails = BuildDetailsImpl(cachedBuild)
                                        // Compare if server side and cache are updated
                                        // If cache's not updated -> update it
                                        repository.build(
                                                cachedBuild.href,
                                                // Don't update cache if server and cache builds are finished
                                                serverBuildDetails.isFinished != cacheBuildDetails.isFinished)
                                    }
                        }
                    }
                    .map { BuildDetailsImpl(it) }
                    .toList()
                    .map { list ->
                        val buildListByBuildTypeIds = mutableMapOf<String, List<BuildDetails>>()
                        favoriteBuildTypeIds.forEach { buildTypeId ->
                            val buildList = list.filter { buildDetails -> buildDetails.buildTypeId == buildTypeId }
                            if (buildList.isEmpty()) return@forEach
                            buildListByBuildTypeIds[buildTypeId] = buildList
                        }
                        buildListByBuildTypeIds
                    }
                    .doOnSuccess { processResults(it) }
                    .map { Result.success() }
                    .onErrorReturn {
                        Log.d("WorkManager", it.toString())
                        Result.retry()
                    }
        }
    }

    private fun processResults(buildListByBuildTypeIds: Map<String, List<BuildDetails>>) {
        buildListByBuildTypeIds.forEach {
            val buildTypeId = it.key
            val buildList = it.value

            buildList.forEach { buildDetails ->
                val contentTitle = "${buildDetails.number} on ${buildDetails.branchName}"
                val contentText = buildDetails.statusText
                val summaryText = buildDetails.buildTypeName

                val notification = NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATIONS_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_done_24px)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setSubText(summaryText)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setGroup(buildTypeId)
                        .setStyle(NotificationCompat.BigTextStyle()
                                .setBigContentTitle(contentTitle))
                        .build()
                NotificationManagerCompat.from(applicationContext).apply {
                    notify(buildDetails.id.hashCode(), notification)
                }
            }
            val buildTypeName = buildList[0].buildTypeName
            val summaryNotification = NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATIONS_CHANNEL_ID)
                    .setContentTitle("You got new notifications")
                    .setSubText(buildTypeName)
                    .setSmallIcon(R.drawable.ic_done_24px)
                    .setGroup(buildTypeId)
                    //set this notification as the summary for the group
                    .setGroupSummary(true)
                    .build()
            NotificationManagerCompat.from(applicationContext).apply {
                notify(buildTypeId.hashCode(), summaryNotification)
            }
        }
    }
}
