package com.example.tutorial.com.example.tutorial.core.service

import android.util.Log
import com.example.tutorial.com.example.tutorial.core.util.NotificationHelper
import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.repository.SensorDataRepository
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {

    @Inject
    lateinit var heartRateMonitor: HeartRateMonitor

    @Inject
    lateinit var sensorDataRepository: SensorDataRepository
    @Inject
    lateinit var notifier: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        Log.d("DataLayerPhone", "DataLayerListenerService created on phone")
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("DataLayerPhone", "onDataChanged() => ${dataEvents.count} events")
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                Log.d("DataLayerPhone", "Received path: $path")
                when (path) {
                    "/heartrate" -> {
                        val heartRate = dataMap.getInt("heartrate")
                        if (heartRate == 0) {
                            Log.d("DataLayerPhone", "Discard HR=0")
                            return@forEach
                        }
                        val isStanding = dataMap.getBoolean("isStanding")
                        Log.d(
                            "DataLayerPhone",
                            "Got single HR from watch => hr=$heartRate, stand=$isStanding"
                        )

                        heartRateMonitor.send(heartRate, isStanding)

                        CoroutineScope(Dispatchers.IO).launch {
                            Log.d("DataLayerPhone", "Inserting single HR record into phone DB")
                            sensorDataRepository.insertSensorData(
                                SensorData(
                                    timestamp = System.currentTimeMillis(),
                                    heartRate = heartRate,
                                    isStanding = isStanding
                                )
                            )
                            Log.d("DataLayerPhone", "Single record saved to phone DB successfully")
                        }
                    }

                    "/alert" -> {
                        val fromPhone = dataMap.getBoolean("origin_phone", false)
                        if (fromPhone) return@forEach

                        val title = dataMap.getString("title")
                        val message = dataMap.getString("message")
                        Log.d("DataLayerPhone", "Alert (remote)  title=$title  msg=$message")
                        notifier.show(1, title ?: "Alert", message ?: "")
                    }

                    "/batch_sync" -> {
                        val heartRatesList =
                            dataMap.getIntegerArrayList("heartRates") ?: arrayListOf()
                        val timestampsList =
                            dataMap.getStringArrayList("timestamps") ?: arrayListOf()
                        val standList = dataMap.getIntegerArrayList("isStanding") ?: arrayListOf()

                        Log.d(
                            "DataLayerPhone",
                            "Received /batch_sync => size=${heartRatesList.size}"
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            for (i in heartRatesList.indices) {
                                val heartRate = heartRatesList[i]
                                if (heartRate == 0) continue
                                val timestampString = timestampsList.getOrNull(i) ?: "0"
                                val timestamp = timestampString.toLongOrNull() ?: 0L
                                val standInt = standList.getOrNull(i) ?: 0
                                val isStanding = standInt == 1

                                sensorDataRepository.insertSensorData(
                                    SensorData(
                                        timestamp = timestamp,
                                        heartRate = heartRate,
                                        isStanding = isStanding,
                                        synced = false
                                    )
                                )
                            }
                            Log.d(
                                "DataLayerPhone",
                                "Batch of ${heartRatesList.size} items saved to phone DB successfully"
                            )
                        }
                    }

                    else -> {
                        Log.d("DataLayerPhone", "Unknown path => $path")
                    }
                }
            }
        }
    }


}
