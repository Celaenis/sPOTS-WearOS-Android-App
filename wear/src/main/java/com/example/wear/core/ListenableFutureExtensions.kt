package com.example.tutorial.com.example.wear.core

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { cancel(true) }
    addListener(
        {
            try {
                cont.resume(get())
            } catch (e: Exception) {
                if (e is CancellationException) cont.cancel(e)
                else cont.resumeWithException(e)
            }
        },
        { it.run() }
    )
}
