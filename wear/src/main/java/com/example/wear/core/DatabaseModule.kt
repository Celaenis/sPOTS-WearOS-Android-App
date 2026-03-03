package com.example.tutorial.com.example.wear.core

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.tutorial.data.local.AppDatabase
import com.example.tutorial.data.local.SensorDataDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "watch_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSensorDataDao(db: AppDatabase): SensorDataDao = db.sensorDataDao()
}
