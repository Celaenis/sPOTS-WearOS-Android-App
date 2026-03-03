package com.example.tutorial.com.example.wear.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.unregisterMeasureCallback
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.repository.SensorDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class HealthServicesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sensorDataRepository: SensorDataRepository
) : MeasureCallback {

    private val passiveMonitoringClient: PassiveMonitoringClient =
        HealthServices.getClient(context).passiveMonitoringClient

    suspend fun startPassiveMonitoring() {
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveDataService::class.java,
            config
        ).await()
    }

    suspend fun stopPassiveMonitoring() {
        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
    }

    private val measureClient: MeasureClient =
        HealthServices.getClient(context).measureClient
    private var measureCallbackRegistered = false

    fun startHeartRateMeasurement() {
        if (measureCallbackRegistered) return
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, this)
        measureCallbackRegistered = true
    }

    suspend fun stopHeartRateMeasurement() {
        if (measureCallbackRegistered) {
            measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, this)
            measureCallbackRegistered = false
        }
    }

    var latestHeartRate: Int = 0
    val _heartRateFlow = MutableSharedFlow<Int>(replay = 1)
    val heartRateFlow: SharedFlow<Int> = _heartRateFlow.asSharedFlow()

    var isTestMode = false
    private var simulatedHeartRate = 0

    fun setSimulatedHeartRate(value: Int) {
        simulatedHeartRate = value
        latestHeartRate = value
        _heartRateFlow.tryEmit(value)
    }

    private var lastActiveHrTs: Long = 0L
    private val activeSampleIntervalMs = 5_000L

    override fun onDataReceived(data: DataPointContainer) {
        if (isTestMode) return
        val now = System.currentTimeMillis()
        if (now - lastActiveHrTs < activeSampleIntervalMs) return
        lastActiveHrTs = now
        val hrPoints = data.getData(DataType.HEART_RATE_BPM)
            .filterIsInstance<SampleDataPoint<Float>>()
        if (hrPoints.isEmpty()) return
        val bpm = hrPoints.first().value.roundToInt()
        latestHeartRate = bpm
        _heartRateFlow.tryEmit(bpm)
        storeMeasurementLocally(bpm, false)
    }

    override fun onAvailabilityChanged(
        dataType: DeltaDataType<*, *>,
        availability: Availability
    ) {
        if (availability is DataTypeAvailability) {
            Log.d("HealthServicesManager", "Availability changed for $dataType: $availability")
        }
    }

    private fun storeMeasurementLocally(bpm: Int, isStanding: Boolean) =
        GlobalScope.launch(Dispatchers.IO) {
            sensorDataRepository.insertSensorData(
                SensorData(
                    timestamp = System.currentTimeMillis(),
                    heartRate = bpm,
                    isStanding = isStanding,
                    synced = false
                )
            )
        }

    suspend fun syncWithPhone() {
        val unsynced = sensorDataRepository.getUnsyncedData()
        if (unsynced.isEmpty()) return
        val req = PutDataMapRequest.create("/batch_sync").apply {
            val hr = ArrayList<Int>()
            val ts = ArrayList<String>()
            val stand = ArrayList<Int>()
            val rowIds = ArrayList<Int>()
            unsynced.forEach {
                hr.add(it.heartRate)
                ts.add(it.timestamp.toString())
                stand.add(if (it.isStanding) 1 else 0)
                rowIds.add(it.id)
            }
            dataMap.putIntegerArrayList("heartRates", hr)
            dataMap.putStringArrayList("timestamps", ts)
            dataMap.putIntegerArrayList("isStanding", stand)
            dataMap.putIntegerArrayList("rowIds", rowIds)
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(req).await()
        sensorDataRepository.markAsSynced(unsynced.map { it.id })
    }

    suspend fun sendToHandheldDevice(heartRate: Int, isStanding: Boolean) {
        if (heartRate == 0) return
        val req = PutDataMapRequest.create("/heartrate").apply {
            dataMap.putInt("heartrate", heartRate)
            dataMap.putBoolean("isStanding", isStanding)
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(req).await()
    }

    fun sendWatchNotification(title: String, message: String) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "alert_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(chId, "Alerts", NotificationManager.IMPORTANCE_HIGH)
            mgr.createNotificationChannel(ch)
        }
        val n: Notification = NotificationCompat.Builder(context, chId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .build()
        mgr.notify(1, n)
    }
}
