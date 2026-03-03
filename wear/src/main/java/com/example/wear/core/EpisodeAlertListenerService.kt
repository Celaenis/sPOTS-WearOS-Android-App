package com.example.tutorial.com.example.wear.core

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EpisodeAlertListenerService : WearableListenerService() {

    @Inject
    lateinit var hs: HealthServicesManager

    override fun onDataChanged(buf: DataEventBuffer) {
        buf.forEach { ev ->
            if (ev.type != DataEvent.TYPE_CHANGED) return@forEach
            if (ev.dataItem.uri.path != "/alert") return@forEach

            val dm = DataMapItem.fromDataItem(ev.dataItem).dataMap
            val fromPhone = dm.getBoolean("origin_phone", false)
            if (!fromPhone) return@forEach

            val title = dm.getString("title") ?: "POTS episode"
            val msg = dm.getString("message") ?: ""
            Log.d("AlertListenerWear", "Showing watch alert  $msg")
            hs.sendWatchNotification(title, msg)
        }
    }
}
