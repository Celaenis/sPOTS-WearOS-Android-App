package com.example.tutorial.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val sensorRepo: SensorDataRepository,
    private val episodeRepo: EpisodeRepository,
    private val symptomRepo: SymptomRepository,
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {

    suspend fun push() {
        val uid = auth.currentUser?.uid ?: return

        val unsSensors = sensorRepo.getUnsyncedData()
        if (unsSensors.isNotEmpty()) {
            fs.runBatch { b ->
                val col = col(uid, "sensorData")
                unsSensors.forEach {
                    b.set(
                        col.document(),
                        mapOf(
                            "timestamp" to it.timestamp,
                            "heartRate" to it.heartRate,
                            "isStanding" to it.isStanding
                        )
                    )
                }
            }.await()
            sensorRepo.markAsSynced(unsSensors.map { it.id })
        }

        val unsEpisodes = episodeRepo.unsynced()
        if (unsEpisodes.isNotEmpty()) {
            fs.runBatch { b ->
                val col = col(uid, "episodes")
                unsEpisodes.forEach {
                    b.set(
                        col.document(),
                        mapOf(
                            "start" to it.startTimestamp,
                            "end" to it.endTimestamp,
                            "baselineHr" to it.baselineHr,
                            "peakHr" to it.peakHr,
                            "delta" to it.delta
                        )
                    )
                }
            }.await()
            episodeRepo.markSynced(unsEpisodes.map { it.id })
        }

        symptomRepo.pushUnsynced()
    }

    private fun col(uid: String, name: String) =
        fs.collection("users").document(uid).collection(name)
}
