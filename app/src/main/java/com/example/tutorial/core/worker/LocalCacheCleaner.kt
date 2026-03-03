package com.example.tutorial.com.example.tutorial.core.worker

import com.example.tutorial.data.local.EpisodeDao
import com.example.tutorial.data.local.SensorDataDao
import com.example.tutorial.data.local.SymptomDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCacheCleaner @Inject constructor(
    private val sensorDao: SensorDataDao,
    private val symptomDao: SymptomDao,
    private val episodeDao: EpisodeDao
) {
    suspend fun clear() {
        sensorDao.clearAll()
        symptomDao.clearAll()
        episodeDao.clearAll()
    }
}
