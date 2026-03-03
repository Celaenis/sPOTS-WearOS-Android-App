package com.example.tutorial.com.example.tutorial.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tutorial.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result =
        runCatching { syncRepository.push() }
            .fold({ Result.success() }, { Result.retry() })
}
