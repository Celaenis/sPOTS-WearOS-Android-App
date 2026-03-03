package com.example.tutorial.com.example.wear.core

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PassiveDataService : PassiveListenerService() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        if (healthServicesManager.isTestMode) {
            return
        }

        val heartRatePoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        if (heartRatePoints.isNotEmpty()) {
            val hrValue = heartRatePoints.first().value.toInt()
            if (hrValue == 0) return
            healthServicesManager.latestHeartRate = hrValue
            healthServicesManager._heartRateFlow.tryEmit(hrValue)
            Log.d("PassiveDataService", "Passive HR received: $hrValue BPM")
        } else {
            Log.d("PassiveDataService", "No heart rate data in this passive update.")
        }
    }
}
