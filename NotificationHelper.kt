package com.ia.quotesapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ia.quotesapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showNotification(
        title: String,
        msg: String,
        channelId: String,
        pendingIntent: PendingIntent? = null,
        autoCancel : Boolean = false
    ) {

        val notification =
            NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setSmallIcon(R.drawable.ic_app_logo)
                .run {
                    pendingIntent?.let {
                        return@run setContentIntent(it).setAutoCancel(true)
                    }
                    return@run setAutoCancel(autoCancel)
                }
                .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }


    fun createNotificationChannelIfNeeded(
        id: String,
        name: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        description: String? = null
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = notificationManager.getNotificationChannel(id)
        if (channel == null) {
            createNotificationChannel(id, name, importance, description)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        id: String,
        name: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        description: String? = null
    ) {
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val notificationPermission = Manifest.permission.POST_NOTIFICATIONS

        var isPermissionRationaleSeen = false
    }
}