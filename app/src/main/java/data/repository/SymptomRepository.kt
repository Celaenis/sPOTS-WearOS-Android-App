package com.example.tutorial.data.repository

import com.example.tutorial.data.local.SymptomDao
import com.example.tutorial.data.local.SymptomEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymptomRepository @Inject constructor(
    private val dao: SymptomDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private fun dayInt(millis: Long): Int =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }

    suspend fun getForDay(day: Int) = dao.getByDay(day)
    suspend fun daysWithSymptoms(from: Int, to: Int) = dao.getDaysWithSymptoms(from, to)
    suspend fun range(from: Long, to: Long): List<SymptomEntity> = dao.getRange(from, to)

    suspend fun insertBatch(epochMillis: Long, map: Map<String, Int>) {
        val day = dayInt(epochMillis)
        dao.deleteDay(day)
        val rows = map.filterValues { it > 0 }.map { (symId, sev) ->
            SymptomEntity(
                day = day,
                millis = epochMillis,
                symptomId = symId,
                severity = sev,
                synced = false
            )
        }
        if (rows.isNotEmpty()) dao.upsertAll(rows)
    }

    suspend fun pullRemoteToLocal() {
        val uid = auth.currentUser?.uid ?: return
        val snap = firestore.collection("users")
            .document(uid)
            .collection("manualSymptoms")
            .get()
            .await()
        if (snap.isEmpty) return
        val rows = snap.documents.mapNotNull { doc ->
            val symptomId = doc.getString("symptomId") ?: return@mapNotNull null
            val severity = (doc.getLong("severity") ?: 0L).toInt()
            val ts = doc.getLong("timestamp") ?: return@mapNotNull null
            if (severity <= 0) return@mapNotNull null
            SymptomEntity(
                day = dayInt(ts),
                millis = ts,
                symptomId = symptomId,
                severity = severity,
                synced = true
            )
        }
        if (rows.isNotEmpty()) dao.upsertAll(rows)
    }

    suspend fun pushUnsynced() {
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return
        val uid = auth.currentUser?.uid ?: return
        val col = firestore.collection("users")
            .document(uid)
            .collection("manualSymptoms")
        firestore.runBatch { b ->
            unsynced.forEach { row ->
                b.set(
                    col.document(),
                    mapOf(
                        "symptomId" to row.symptomId,
                        "severity" to row.severity,
                        "timestamp" to row.millis
                    )
                )
            }
        }.await()
        dao.markSynced(unsynced.map { it.id })
    }
}
