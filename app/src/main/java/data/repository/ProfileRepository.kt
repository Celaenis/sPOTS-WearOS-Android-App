package com.example.tutorial.data.repository

import com.example.tutorial.com.example.tutorial.core.worker.LocalCacheCleaner
import com.example.tutorial.com.example.tutorial.domain.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cacheCleaner: LocalCacheCleaner
) {

    private fun profileDoc() = auth.currentUser?.let { user ->
        firestore.collection("users")
            .document(user.uid)
            .collection("profile")
            .document("main")
    }

    fun profileFlow(): Flow<UserProfile?> = callbackFlow {
        val doc = profileDoc() ?: run { close(); return@callbackFlow }
        val reg = doc.addSnapshotListener { snap, _ ->
            val p = snap?.toObject(UserProfile::class.java)
            trySend(p)
        }
        awaitClose { reg.remove() }
    }

    suspend fun updateProfile(name: String, age: Int): Result<Unit> = runCatching {
        profileDoc()?.update(
            mapOf(
                "displayName" to name,
                "age" to age
            )
        )?.await() ?: error("No user")
    }


    suspend fun deleteAccount(password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No user in session")
        val uid = user.uid

        val cred = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(cred).await()

        val root = firestore.collection("users").document(uid)
        val colNames = listOf("sensorData", "manualSymptoms", "profile", "episodes")

        val docsToDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()
        for (cName in colNames) {
            val snap = root.collection(cName).get().await()
            docsToDelete += snap.documents.map { it.reference }
        }

        firestore.runBatch { b ->
            docsToDelete.forEach { b.delete(it) }
            b.delete(root)
        }.await()

        user.delete().await()

        cacheCleaner.clear()
    }
}
