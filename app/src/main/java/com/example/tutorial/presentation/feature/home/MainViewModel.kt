package com.example.tutorial.com.example.tutorial.presentation.feature.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorial.com.example.tutorial.core.service.HeartRateMonitor
import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.repository.EpisodeRepository
import com.example.tutorial.data.repository.SensorDataRepository
import com.example.tutorial.data.repository.SyncRepository
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HealthState(
    val heartRate: Int = 0,
    val isStanding: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SensorDataRepository,
    private val episodeRepo: EpisodeRepository,
    private val syncRepo: SyncRepository,
    @ApplicationContext private val appContext: Context,
    private val heartRateMonitor: HeartRateMonitor
) : ViewModel() {

    private val _recentHeartRatesLiveData = MutableLiveData<List<SensorData>>()
    val recentHeartRatesLiveData: LiveData<List<SensorData>> = _recentHeartRatesLiveData

    fun loadRecentHeartRates() {
        Log.d("MainViewModelPhone", "loadRecentHeartRates() called")
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getRecentHeartBeats()
            Log.d("MainViewModelPhone", "Loaded ${data.size} recent entries from DB")
            withContext(Dispatchers.Main) {
                _recentHeartRatesLiveData.value = data
            }
        }
    }

    private val _health = MutableStateFlow(HealthState())
    val health: StateFlow<HealthState> = _health.asStateFlow()

    init {
        viewModelScope.launch {
            heartRateMonitor.receive().collect { pkt ->
                _health.value = HealthState(pkt.heartRate, pkt.isStanding)
            }
        }
    }


    fun refresh() {
        Log.d("MainViewModelPhone", "refresh() => push unsynced data to Firestore & reload DB")
        viewModelScope.launch(Dispatchers.IO) {
            syncRepo.push()

            val data = repository.getRecentHeartBeats()
            Log.d("MainViewModelPhone", "After sync, loaded ${data.size} recent entries from DB")
            withContext(Dispatchers.Main) {
                _recentHeartRatesLiveData.value = data
            }
        }
    }


    fun requestWatchDataSync() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(
                "MainViewModelPhone",
                "requestWatchDataSync() => sending /trigger_sync msg to watch"
            )

            val nodeClient = Wearable.getNodeClient(appContext)
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.d("MainViewModelPhone", "No watch nodes found, can't request sync")
                return@launch
            }

            val nodeId = nodes[0].id
            val msgClient = Wearable.getMessageClient(appContext)
            val path = "/trigger_sync"
            try {
                msgClient.sendMessage(nodeId, path, ByteArray(0)).await()
                Log.d(
                    "MainViewModelPhone",
                    "Message to watch sent => watch should do syncWithPhone()"
                )
            } catch (e: Exception) {
                Log.e("MainViewModelPhone", "Failed sending message to watch", e)
            }
        }
    }


    fun syncAllInOne() {
        Log.d(
            "MainViewModelPhone",
            "syncAllInOne() => 1) request watch sync, 2) wait, 3) push phone->firestore, 4) reload DB"
        )
        viewModelScope.launch(Dispatchers.IO) {

            requestWatchDataSyncInternal()

            delay(2000L)

            syncRepo.push()

            val data = repository.getRecentHeartBeats()
            Log.d(
                "MainViewModelPhone",
                "After watch & phone sync, loaded ${data.size} entries from DB"
            )
            withContext(Dispatchers.Main) {
                _recentHeartRatesLiveData.value = data
            }
        }
    }

    private suspend fun requestWatchDataSyncInternal() {
        Log.d(
            "MainViewModelPhone",
            "requestWatchDataSyncInternal => sending /trigger_sync msg to watch"
        )
        val nodeClient = Wearable.getNodeClient(appContext)
        val nodes = nodeClient.connectedNodes.await()
        if (nodes.isEmpty()) {
            Log.d("MainViewModelPhone", "No watch nodes found, can't request sync")
            return
        }

        val nodeId = nodes[0].id
        val msgClient = Wearable.getMessageClient(appContext)
        val path = "/trigger_sync"
        try {
            msgClient.sendMessage(nodeId, path, ByteArray(0)).await()
            Log.d("MainViewModelPhone", "Message to watch sent => watch does syncWithPhone()")
        } catch (e: Exception) {
            Log.e("MainViewModelPhone", "Failed sending message to watch", e)
        }
    }


}
