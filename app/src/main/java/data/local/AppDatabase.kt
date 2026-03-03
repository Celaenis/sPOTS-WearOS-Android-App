package com.example.tutorial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SensorData::class,
        SymptomEntity::class,
        EpisodeEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorDataDao(): SensorDataDao
    abstract fun symptomDao(): SymptomDao
    abstract fun episodeDao(): EpisodeDao
}
