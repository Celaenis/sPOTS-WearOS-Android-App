package com.example.tutorial.com.example.tutorial.core.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class HeartRateData(val heartRate: Int, val isStanding: Boolean)

@Singleton
class HeartRateMonitor @Inject constructor() {

    private val dataPoints = MutableSharedFlow<HeartRateData>(
        replay = 0,
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun receive(): SharedFlow<HeartRateData> = dataPoints.asSharedFlow()

    fun send(heartRate: Int, isStanding: Boolean) {
        dataPoints.tryEmit(HeartRateData(heartRate, isStanding))
    }
}