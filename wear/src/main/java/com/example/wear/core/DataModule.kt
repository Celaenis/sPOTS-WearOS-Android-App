package com.example.tutorial.com.example.wear.core

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {


    @Provides
    @Singleton
    fun provideMotionDetector(@ApplicationContext context: Context): MotionDetector {
        return MotionDetector(context)
    }
}
