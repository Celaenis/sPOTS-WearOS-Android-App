package com.example.tutorial.com.example.tutorial.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tutorial.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val channelId = "alert_channel"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "Alerts", NotificationManager.IMPORTANCE_HIGH
            )
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(ch)
        }
    }

    fun show(id: Int, title: String, msg: String) {
        val n = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(msg)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(id, n)
    }
}
