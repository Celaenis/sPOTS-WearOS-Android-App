package com.example.tutorial.data.repository

import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.local.SensorDataDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataRepository @Inject constructor(
    private val sensorDataDao: SensorDataDao
) {

    suspend fun insertSensorData(data: SensorData) {
        sensorDataDao.insert(data)
    }

    suspend fun insertAll(list: List<SensorData>) =
        sensorDataDao.insertAll(list)

    suspend fun getUnsyncedData(): List<SensorData> {
        return sensorDataDao.getUnsyncedData()
    }

    suspend fun markAsSynced(ids: List<Int>) {
        sensorDataDao.markAsSynced(ids)
    }

    suspend fun getRecentHeartBeats(): List<SensorData> {
        return sensorDataDao.getLatestSensorData()
    }

    suspend fun range(from: Long, to: Long) =
        sensorDataDao.getRange(from, to)

    suspend fun pullRemoteToLocal(
        auth: FirebaseAuth,
        fs: FirebaseFirestore
    ) {
        val uid = auth.currentUser?.uid ?: return
        val snap = fs.collection("users")
            .document(uid)
            .collection("sensorData")
            .get()
            .await()

        if (snap.isEmpty) return

        val rows = snap.documents.mapNotNull { d ->
            val ts = d.getLong("timestamp") ?: return@mapNotNull null
            val bpm = (d.getLong("heartRate") ?: 0L).toInt()
            val st = d.getBoolean("isStanding") ?: false
            SensorData(
                timestamp = ts,
                heartRate = bpm,
                isStanding = st,
                synced = true
            )
        }
        if (rows.isNotEmpty()) sensorDataDao.insertAll(rows)
    }
}


