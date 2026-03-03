package com.example.tutorial.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val heartRate: Int,
    val isStanding: Boolean,
    val synced: Boolean = false
)
