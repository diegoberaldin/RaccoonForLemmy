package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import java.util.Collections.max
import com.github.diegoberaldin.raccoonforlemmy.core.resources.R as resourcesR

internal class CheckNotificationWorker(
    private val context: Context,
    parameters: WorkerParameters,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Worker(context, parameters) {
    private val inboxCoordinator by KoinJavaComponent.inject<InboxCoordinator>(InboxCoordinator::class.java)
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun doWork(): Result {
        scope.launch {
            inboxCoordinator.updateUnreadCount()
            val unread = inboxCoordinator.totalUnread.value
            if (unread > 0) {
                sendNotification(unread)
            }
        }

        return Result.success()
    }

    @SuppressLint("StringFormatInvalid")
    private fun sendNotification(count: Int) {
        val intent =
            Intent(
                context,
                Class.forName("com.github.diegoberaldin.raccoonforlemmy.android.MainActivity"),
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val title = context.getString(R.string.inbox_notification_title)
        val content = context.getString(R.string.inbox_notification_content, count)
        val notification =
            Notification.Builder(context, NotificationConstants.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(resourcesR.drawable.ic_monochrome)
                .setContentIntent(pendingIntent)
                .setNumber(count)
                .build()
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = getNextNotificationId()
        notificationManager.notify(
            NotificationConstants.NOTIFICATION_TAG,
            notificationId,
            notification
        )
    }

    private fun getNextNotificationId(): Int {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications =
            notificationManager.activeNotifications.filter { it.tag == context.packageName }
        return if (activeNotifications.isEmpty()) {
            0
        } else {
            max(activeNotifications.map { it.id }) + 1
        }
    }
}
