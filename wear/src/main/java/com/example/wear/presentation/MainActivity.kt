package com.example.wear.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import com.example.tutorial.com.example.wear.core.HealthServicesManager
import com.example.tutorial.com.example.wear.core.MonitoringService
import com.example.tutorial.com.example.wear.core.MotionDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var healthServicesManager: HealthServicesManager
    @Inject
    lateinit var motionDetector: MotionDetector

    private val permissions = mutableListOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.INTERNET
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantMap ->
            if (grantMap.values.all { it }) {
                startMonitoringService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        motionDetector.start()
        setContent {
            MainScreen()
        }
    }

    private fun checkPermissions() {
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            requestPermissionLauncher.launch(needed.toTypedArray())
        } else {
            startMonitoringService()
        }
    }

    private fun startMonitoringService() {
        val serviceIntent = Intent(this, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private suspend fun simulateEpisode() {
        repeat(30) {
            healthServicesManager.setSimulatedHeartRate(70)
            healthServicesManager.sendToHandheldDevice(70, isStanding = false)
            delay(1000)
        }

        healthServicesManager.setSimulatedHeartRate(110)
        healthServicesManager.sendToHandheldDevice(110, isStanding = true)
        delay(2000)

        healthServicesManager.setSimulatedHeartRate(125)
        healthServicesManager.sendToHandheldDevice(125, isStanding = true)
        delay(2000)

        healthServicesManager.setSimulatedHeartRate(80)
        healthServicesManager.sendToHandheldDevice(80, isStanding = false)
    }


    @Composable
    fun MainScreen() {
        val coroutineScope = rememberCoroutineScope()
        val currentHr = remember { mutableStateOf(healthServicesManager.latestHeartRate) }
        val isStanding = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            healthServicesManager.heartRateFlow.collect { bpm ->
                currentHr.value = bpm
            }
        }
        LaunchedEffect(Unit) {
            motionDetector.isStandingFlow.collect { isStanding.value = it }
        }

        var passiveRunning by remember { mutableStateOf(false) }
        var activeRunning by remember { mutableStateOf(false) }

        val listState = rememberScalingLazyListState()

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "HR: ${currentHr.value} BPM",
                    style = MaterialTheme.typography.title2
                )

            }
            item {
                Text(
                    text = if (isStanding.value) "Standing" else "Sitting",
                    style = MaterialTheme.typography.title2
                )
                Spacer(Modifier.height(30.dp))
            }
//            item {
//                Button(
//                    onClick = {
//                        coroutineScope.launch {
//                            if (!passiveRunning) {
//                                healthServicesManager.startPassiveMonitoring()
//                                passiveRunning = true
//                            } else {
//                                healthServicesManager.stopPassiveMonitoring()
//                                passiveRunning = false
//                            }
//                        }
//                    }
//                ) {
//                    Text(if (passiveRunning) "Stop Passive" else "Start Passive")
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//            }
//            item {
//                Button(
//                    onClick = {
//                        coroutineScope.launch {
//                            if (!activeRunning) {
//                                healthServicesManager.startHeartRateMeasurement()
//                                activeRunning = true
//                            } else {
//                                healthServicesManager.stopHeartRateMeasurement()
//                                activeRunning = false
//                            }
//                        }
//                    }
//                ) {
//                    Text(if (activeRunning) "Stop Active" else "Start Active")
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//            }
//
//            item {
//                Button(onClick = {
//                    healthServicesManager.isTestMode = !healthServicesManager.isTestMode
//                }) {
//                    Text(
//                        if (healthServicesManager.isTestMode)
//                            "Disable Test Mode"
//                        else
//                            "Enable Test Mode"
//                    )
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//            }
//
//            item {
//                Button(onClick = {
//                    if (healthServicesManager.isTestMode) {
//                        healthServicesManager.setSimulatedHeartRate((60..120).random())
//                    }
//                }) {
//                    Text("Simulate Random HR (TestMode)")
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//            }
//
//            item {
//                Button(onClick = {
//                    coroutineScope.launch {
//                        healthServicesManager.syncWithPhone()
//                    }
//                }) {
//                    Text("Sync Now")
//                }
//                Spacer(modifier = Modifier.height(10.dp))
//            }
            item {
                Button(onClick = {
                    coroutineScope.launch {
                        simulateEpisode()
                    }
                }) { Text("Simulate POTS episode") }

            }
        }
    }

}
