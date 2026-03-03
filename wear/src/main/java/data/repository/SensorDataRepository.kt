package com.example.tutorial.data.repository

import com.example.tutorial.data.local.SensorData
import com.example.tutorial.data.local.SensorDataDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorDataRepository @Inject constructor(
    private val sensorDataDao: SensorDataDao
) {
    suspend fun insertSensorData(data: SensorData) {
        if (data.heartRate == 0) return
        sensorDataDao.insert(data)
    }

    suspend fun getUnsyncedData() = sensorDataDao.getUnsyncedData()
    suspend fun markAsSynced(ids: List<Int>) = sensorDataDao.markAsSynced(ids)
}
