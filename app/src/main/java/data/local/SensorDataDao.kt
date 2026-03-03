package com.example.tutorial.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sensorData: SensorData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<SensorData>)

    @Query("SELECT * FROM sensor_data WHERE synced = 0")
    suspend fun getUnsyncedData(): List<SensorData>

    @Query("UPDATE sensor_data SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT 20")
    suspend fun getLatestSensorData(): List<SensorData>

    @Query("DELETE FROM sensor_data")
    suspend fun clearAll()

    @Query(
        "SELECT * FROM sensor_data " +
                "WHERE timestamp BETWEEN :from AND :to " +
                "ORDER BY timestamp ASC"
    )
    suspend fun getRange(from: Long, to: Long): List<SensorData>

}
