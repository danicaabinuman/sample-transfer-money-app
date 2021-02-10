package com.unionbankph.corporate.app.helper

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R

class NotificationHelper(base: Context) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null

    val manager: NotificationManager?
        get() {
            if (mManager == null) {
                mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            return mManager
        }

    fun channelNotification(
        channelId: String,
        title: String?,
        contextText: String?,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val notificationCompat = NotificationCompat.Builder(applicationContext, channelId).apply {
            setContentIntent(pendingIntent)
            setSmallIcon(R.drawable.ic_ub_notification)
            setContentTitle(title)
            setContentText(contextText)
            setAutoCancel(true)
            setShowWhen(true)
            setColorized(true)
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(longArrayOf(1000, 1000))
//            color = ContextCompat.getColor(applicationContext, R.color.colorTransparent)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }
        notificationCompat.priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            NotificationCompat.PRIORITY_HIGH
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(channelId)
        }
        return notificationCompat
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(channelId: String) {
        val channel =
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                vibrationPattern = longArrayOf(1000, 1000)
            }
        manager?.createNotificationChannel(channel)
    }

    private fun getPendingIntent(intent: Intent): PendingIntent? {
        val taskStackBuilder = TaskStackBuilder.create(this)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val channelName = "Channel Name"
    }
}
