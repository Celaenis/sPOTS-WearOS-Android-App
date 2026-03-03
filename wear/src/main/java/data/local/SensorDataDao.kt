package com.example.tutorial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sensorData: SensorData)

    @Query("SELECT * FROM sensor_data WHERE synced = 0")
    suspend fun getUnsyncedData(): List<SensorData>

    @Query("UPDATE sensor_data SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)
}
