package com.example.tutorial.com.example.wear.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.wear.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager
    @Inject
    lateinit var motionDetector: MotionDetector

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var hrJob: Job? = null
    private var isStanding = false
    private lateinit var wake: PowerManager.WakeLock

    companion object {
        private const val NID = 1
        private const val CH = "monitoring_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        startForegroundServiceNotification()
        scope.launch { healthServicesManager.startPassiveMonitoring() }
        healthServicesManager.startHeartRateMeasurement()
        startMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            stopMonitoring()
            healthServicesManager.stopPassiveMonitoring()
            healthServicesManager.stopHeartRateMeasurement()
            releaseWakeLock()
        }
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MonitoringService::WL")
        wake.acquire()
    }

    private fun releaseWakeLock() {
        if (wake.isHeld) wake.release()
    }

    private fun startForegroundServiceNotification() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(CH, "Monitoring", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n: Notification = NotificationCompat.Builder(this, CH)
            .setContentTitle("sPOTS")
            .setContentText("Monitoring heart-rate")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            startForeground(NID, n, FOREGROUND_SERVICE_TYPE_HEALTH)
        else
            startForeground(NID, n)
    }

    private fun startMonitoring() {
        motionDetector.start()
        scope.launch {
            motionDetector.isStandingFlow.collect { isStanding = it }
        }
        scope.launch {
            while (isActive) {
                delay(5_000)
                healthServicesManager.sendToHandheldDevice(
                    healthServicesManager.latestHeartRate,
                    isStanding
                )
            }
        }
    }

    private suspend fun stopMonitoring() {
        motionDetector.stop()
        hrJob?.cancel()
    }
}
