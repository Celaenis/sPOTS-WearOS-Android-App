package com.example.tutorial.com.example.tutorial.core.di

import android.content.Context
import androidx.room.Room
import com.example.tutorial.data.local.AppDatabase
import com.example.tutorial.data.local.EpisodeDao
import com.example.tutorial.data.local.SensorDataDao
import com.example.tutorial.data.local.SymptomDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideSensorDataDao(db: AppDatabase): SensorDataDao = db.sensorDataDao()

    @Provides
    fun provideSymptomDao(db: AppDatabase): SymptomDao = db.symptomDao()
    @Provides
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()

}
