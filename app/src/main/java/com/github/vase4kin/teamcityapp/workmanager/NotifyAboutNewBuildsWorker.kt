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
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
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
                        // if last build is empty just fetch last 10 ones, but still have default value like 20?
                        val lastBuild = favoriteBuildTypes.getValue(buildTypeId).sinceBuild
                        val locator = if (lastBuild.isEmpty()) {
                            "status:FAILURE,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10"
                        } else {
                            "status:FAILURE,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,sinceBuild:$lastBuild,count:10"
                        }
                        repository.listBuilds(buildTypeId, locator, true)
                    }
                    .flatMap { Observable.fromIterable(it.objects) }
                    .flatMapSingle { serverBuild ->
                        // TODO: Move to ext function
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
                    .map { buildList ->
                        val buildTypeBuilds = mutableMapOf<String, Set<BuildDetails>>()
                        favoriteBuildTypeIds.forEach { buildTypeId ->
                            val buildsByBuildTypeId = buildList.filter { buildDetails -> buildDetails.buildTypeId == buildTypeId }
                            buildTypeBuilds[buildTypeId] = buildsByBuildTypeId.toSet()
                        }
                        buildTypeBuilds
                    }
                    .doOnSuccess { updateLastBuildForFavoriteBuildType(favoriteBuildTypes, it) }
                    .map { list -> createNotifications(list) }
                    .doOnSuccess { sendNotifications(it) }
                    .map { Result.success() }
                    .onErrorReturn {
                        Log.d("WorkManager", it.toString())
                        Result.retry()
                    }
        }
    }

    private fun updateLastBuildForFavoriteBuildType(
            favoriteBuildTypes: MutableMap<String, UserAccount.FavoriteBuildTypeInfo>,
            buildTypeBuilds: MutableMap<String, Set<BuildDetails>>) {
        buildTypeBuilds.forEach {
            val buildTypeId = it.key
            val builds = it.value
            if (builds.isEmpty()) return@forEach
            val lastBuildId = builds.last().id
            val updatedInfo = favoriteBuildTypes.getValue(buildTypeId).copy(sinceBuild = lastBuildId)
            favoriteBuildTypes[buildTypeId] = updatedInfo
        }
    }

    private fun createNotifications(buildTypeBuilds: MutableMap<String, Set<BuildDetails>>): Set<BuildTypeNotification> {
        val notifications = mutableSetOf<BuildTypeNotification>()
        buildTypeBuilds.forEach {
            val buildTypeId = it.key
            val builds = it.value
            if (builds.isEmpty()) return@forEach
            val buildTypeName = builds.last().buildTypeName
            val buildTypeNotification = BuildTypeNotification(buildTypeId, buildTypeName)
            buildTypeNotification.buildNotifications.addAll(builds.map { buildDetails -> buildDetails.toNotification() })
            notifications.add(buildTypeNotification)
        }
        return notifications.toSet()
    }

    private fun sendNotifications(notifications: Set<BuildTypeNotification>) {
        notifications.forEach { buildTypeNotification ->
            buildTypeNotification.buildNotifications.forEach { notification ->
                sendNotification(notification)
            }
            sendSummaryNotification(buildTypeNotification)
        }
    }

    private fun sendNotification(buildNotification: BuildNotification) {
        val notification = NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_done_24px)
                .setContentTitle(buildNotification.contentTitle)
                .setContentText(buildNotification.contentText)
                .setSubText(buildNotification.summaryText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(buildNotification.groupId)
                .setStyle(NotificationCompat.BigTextStyle()
                        .setBigContentTitle(buildNotification.contentTitle))
                .build()
        NotificationManagerCompat.from(applicationContext).apply {
            notify(buildNotification.id, notification)
        }
    }

    private fun sendSummaryNotification(buildTypeNotification: BuildTypeNotification) {
        val summaryNotification = NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATIONS_CHANNEL_ID)
                .setContentTitle("You got new buildNotifications")
                .setSubText(buildTypeNotification.text)
                .setSmallIcon(R.drawable.ic_done_24px)
                .setGroup(buildTypeNotification.groupId)
                .setGroupSummary(true)
                .build()
        NotificationManagerCompat.from(applicationContext).apply {
            notify(buildTypeNotification.groupId.hashCode(), summaryNotification)
        }
    }

    private data class BuildTypeNotification(val groupId: String,
                                             val text: String) {
        internal val buildNotifications: MutableSet<BuildNotification> = mutableSetOf()
    }

    private data class BuildNotification(
            val id: Int,
            val contentTitle: String,
            val contentText: String,
            val summaryText: String,
            val groupId: String)

    private fun BuildDetails.toNotification() = BuildNotification(
            this.id.hashCode(),
            "${this.number} on ${this.branchName}",
            this.statusText,
            this.buildTypeName,
            this.buildTypeId)
}
