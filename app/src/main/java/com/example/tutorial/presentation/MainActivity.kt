package com.example.tutorial.com.example.tutorial.presentation

//import CloudSyncWorker
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tutorial.com.example.tutorial.core.worker.PushWorker
import com.example.tutorial.com.example.tutorial.presentation.theme.TutorialTheme
import com.example.tutorial.com.example.tutorial.presentation.components.BottomBar
import com.example.tutorial.com.example.tutorial.presentation.navigation.NavigationGraph
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsGranted ->
            if (permissionsGranted.values.all { it }) {
                initializeHeartRateMonitor()
            } else {
            }
        }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        scheduleCloudSync(this)

        setContent {
            TutorialTheme {
                val navController = rememberNavController()
                val currentRoute = navController
                    .currentBackStackEntryAsState().value
                    ?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute !in listOf("login")) {
                            BottomBar(currentRoute) { item ->
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                    }
                ) {
                    NavigationGraph(navController)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            initializeHeartRateMonitor()
        }
    }

    fun scheduleCloudSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<PushWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PushWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun initializeHeartRateMonitor() {
    }
}
