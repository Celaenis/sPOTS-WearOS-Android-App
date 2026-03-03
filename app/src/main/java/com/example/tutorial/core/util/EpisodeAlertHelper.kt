package com.example.tutorial.com.example.tutorial.core.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.tutorial.data.local.EpisodeEntity
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("ObsoleteSdkInt")
@Singleton
class EpisodeAlertHelper @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val notifier: NotificationHelper
) {
    private val notifMgr by lazy {
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val channelId = "episode_alerts"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "POTS Episodes",
                NotificationManager.IMPORTANCE_HIGH
            )
            notifMgr.createNotificationChannel(ch)
        }
    }

    fun fire(ep: EpisodeEntity) {
        val msg = "HR increased by +${ep.delta} bpm  (peak ${ep.peakHr})"
        showLocal(msg)
        mirrorToWatch(msg)
    }

    private fun showLocal(txt: String) =
        notifier.show(
            System.currentTimeMillis().toInt(),
            "Alert! POTS-like episode detected", txt
        )

    private fun mirrorToWatch(txt: String) {
        val req = PutDataMapRequest.create("/alert").apply {
            dataMap.putString("title", "Alert! POTS-like episode detected")
            dataMap.putString("message", txt)
            dataMap.putBoolean("origin_phone", true)
        }.asPutDataRequest().setUrgent()

        Wearable.getDataClient(ctx).putDataItem(req)
    }

}
