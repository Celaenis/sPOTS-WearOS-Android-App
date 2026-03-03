package com.example.tutorial

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.example.tutorial.com.example.tutorial.core.util.EpisodeDetector
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var episodeDetector: EpisodeDetector

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        episodeDetector.hashCode()
    }
}

