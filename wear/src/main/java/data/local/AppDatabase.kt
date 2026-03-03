package com.example.tutorial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SensorData::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao
}
