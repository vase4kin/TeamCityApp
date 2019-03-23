package com.github.vase4kin.teamcityapp.workmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.github.vase4kin.teamcityapp.utils.DEFAULT_NOTIFICATIONS_CHANNEL_ID
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DEFAULT_LOCATOR = "status:FAILURE,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:1"
private const val SINCE_BUILD_LOCATOR = "status:FAILURE,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,sinceBuild:%s,count:10"

const val UNIQUE_WORKER_ID = "UNIQUE_WORKER_ID"

class BuildNotificationsWorker(
        appContext: Context, workerParams: WorkerParameters
) : RxWorker(appContext, workerParams) {

    @Inject
    internal lateinit var sharedUserStorage: SharedUserStorage

    @Inject
    internal lateinit var repository: Repository

    override fun createWork(): Single<ListenableWorker.Result>? {
        (applicationContext as TeamCityApplication).restApiInjector?.inject(this)
                ?: return Single.just(Result.retry())
        val favoriteBuildTypes = sharedUserStorage.activeUser.favoriteBuildTypes
        return if (favoriteBuildTypes.isEmpty()) {
            Single.just<Result>(ListenableWorker.Result.success())
        } else {
            val favoriteBuildTypeIds = favoriteBuildTypes.keys
            Observable.fromIterable(favoriteBuildTypeIds)
                    .flatMapSingle { buildTypeId ->
                        // if last build is empty just fetch last 10 ones, but still have default value like 20?
                        val lastBuild = favoriteBuildTypes.getValue(buildTypeId).sinceBuild
                        val locator = if (lastBuild.isEmpty()) DEFAULT_LOCATOR else String.format(SINCE_BUILD_LOCATOR, lastBuild)
                        repository.listBuilds(buildTypeId, locator, true)
                    }
                    .flatMap { Observable.fromIterable(it.objects) }
                    .flatMapSingle { serverBuild -> repository.cacheBuild(serverBuild) }
                    .map { BuildDetailsImpl(it) }
                    .toList()
                    .map { buildList ->
                        val buildTypeBuilds = mutableMapOf<String, List<BuildDetails>>()
                        favoriteBuildTypeIds.forEach { buildTypeId ->
                            val buildsByBuildTypeId = buildList.filter { buildDetails -> buildDetails.buildTypeId == buildTypeId }
                            buildTypeBuilds[buildTypeId] = buildsByBuildTypeId
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
            buildTypeBuilds: MutableMap<String, List<BuildDetails>>) {
        buildTypeBuilds.forEach {
            val buildTypeId = it.key
            val builds = it.value
            if (builds.isEmpty()) return@forEach
            val lastBuildId = builds.first().id
            val updatedInfo = favoriteBuildTypes.getValue(buildTypeId).copy(sinceBuild = lastBuildId)
            favoriteBuildTypes[buildTypeId] = updatedInfo
        }
    }

    private fun createNotifications(buildTypeBuilds: MutableMap<String, List<BuildDetails>>): Set<BuildTypeNotification> {
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
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(buildNotification.contentTitle)
                .setContentText(buildNotification.contentText)
                .setSubText(buildNotification.summaryText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(buildNotification.groupId)
                .setStyle(NotificationCompat.BigTextStyle()
                        .setBigContentTitle(buildNotification.contentTitle))
                .setContentIntent(buildNotification.createBuildIntent())
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(applicationContext).apply {
            notify(buildNotification.id, notification)
        }
    }

    private fun sendSummaryNotification(buildTypeNotification: BuildTypeNotification) {
        val summaryNotification = NotificationCompat.Builder(applicationContext, DEFAULT_NOTIFICATIONS_CHANNEL_ID)
                .setContentTitle("You got new buildNotifications")
                .setSubText(buildTypeNotification.text)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(buildTypeNotification.groupId)
                .setGroupSummary(true)
                .setContentIntent(buildTypeNotification.createBuildTypeIntent())
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(applicationContext).apply {
            notify(buildTypeNotification.groupId.hashCode(), summaryNotification)
        }
    }

    private fun BuildNotification.createBuildIntent(): PendingIntent = PendingIntent.getActivities(
            applicationContext,
            0,
            arrayOf(Intent(applicationContext, RootProjectsActivity::class.java),
                    Intent(applicationContext, BuildDetailsActivity::class.java)
                            .putExtra(BundleExtractorValues.BUILD, this.buildDetails.toBuild())
                            .putExtra(BundleExtractorValues.NAME, this.buildDetails.buildTypeName)),
            0)

    private fun BuildTypeNotification.createBuildTypeIntent(): PendingIntent = PendingIntent.getActivities(
            applicationContext,
            0,
            arrayOf(Intent(applicationContext, RootProjectsActivity::class.java),
                    Intent(applicationContext, BuildListActivity::class.java)
                            .putExtra(BundleExtractorValues.NAME, this.text)
                            .putExtra(BundleExtractorValues.ID, this.groupId)),
            0)

    private data class BuildTypeNotification(val groupId: String,
                                             val text: String) {
        internal val buildNotifications: MutableSet<BuildNotification> = mutableSetOf()
    }

    private data class BuildNotification(
            val buildDetails: BuildDetails,
            val id: Int,
            val contentTitle: String,
            val contentText: String,
            val summaryText: String,
            val groupId: String)

    private fun BuildDetails.toNotification() = BuildNotification(
            this,
            this.id.hashCode(),
            "${this.number} on ${this.branchName}",
            this.statusText,
            this.buildTypeName,
            this.buildTypeId)
}

fun WorkManager.scheduleWorkers() {
    val workRequest = PeriodicWorkRequest.Builder(
            BuildNotificationsWorker::class.java, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
            .build()
    this.enqueueUniquePeriodicWork(UNIQUE_WORKER_ID, ExistingPeriodicWorkPolicy.KEEP, workRequest)
}
