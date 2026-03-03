package com.example.tutorial.data.repository

import com.example.tutorial.data.local.EpisodeDao
import com.example.tutorial.data.local.EpisodeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeRepository @Inject constructor(
    private val dao: EpisodeDao,
    private val auth: FirebaseAuth,
    private val fs: FirebaseFirestore
) {
    suspend fun save(entity: EpisodeEntity) = dao.insert(entity)
    suspend fun recent(limit: Int = 10) = dao.recent(limit)
    suspend fun unsynced() = dao.unsynced()
    suspend fun markSynced(ids: List<Int>) = dao.markSynced(ids)
    suspend fun clear() = dao.clearAll()
    suspend fun range(from: Long, to: Long): List<EpisodeEntity> = dao.getRange(from, to)

    suspend fun pullRemoteToLocal() {
        val uid = auth.currentUser?.uid ?: return
        val snap = fs.collection("users")
            .document(uid)
            .collection("episodes")
            .get()
            .await()

        if (snap.isEmpty) return

        val rows = snap.documents.mapNotNull { d ->
            val start = d.getLong("start") ?: return@mapNotNull null
            val end = d.getLong("end") ?: return@mapNotNull null
            EpisodeEntity(
                startTimestamp = start,
                endTimestamp = end,
                baselineHr = (d.getLong("baselineHr") ?: 0L).toInt(),
                peakHr = (d.getLong("peakHr") ?: 0L).toInt(),
                delta = (d.getLong("delta") ?: 0L).toInt(),
                synced = true
            )
        }
        if (rows.isNotEmpty()) dao.insertAll(rows)
    }
}

