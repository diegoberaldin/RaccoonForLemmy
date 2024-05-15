package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val TAG = "InboxNotificationCheck"

class DefaultInboxNotificationChecker(
    private val context: Context,
) : InboxNotificationChecker {

    override val isBackgroundCheckSupported = true
    private var intervalMinutes = 15L

    override fun setPeriod(minutes: Long) {
        intervalMinutes = minutes
    }

    override fun start() {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)

        createNotificationChannel()
        PeriodicWorkRequestBuilder<CheckNotificationWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .addTag(TAG)
            .build().apply {
                WorkManager.getInstance(context).enqueue(this)
            }
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
    }

    private fun createNotificationChannel() {
        val descriptionText = ""
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            NotificationConstants.CHANNEL_ID,
            NotificationConstants.CHANNEL_NAME,
            importance,
        ).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
