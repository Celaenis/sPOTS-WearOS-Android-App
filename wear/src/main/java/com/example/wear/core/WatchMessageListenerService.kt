package com.example.tutorial.com.example.wear.core

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WatchMessageListenerService : WearableListenerService() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    override fun onCreate() {
        super.onCreate()
        Log.d("WatchMsgService", "WatchMessageListenerService created")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        Log.d("WatchMsgService", "onMessageReceived: path=$path")
        when (path) {
            "/trigger_sync" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(
                        "WatchMsgService",
                        "Phone requested watch -> phone sync, calling syncWithPhone()"
                    )
                    healthServicesManager.syncWithPhone()
                }
            }

            else -> {
                Log.d("WatchMsgService", "Unknown message path=$path")
            }
        }
    }


}
