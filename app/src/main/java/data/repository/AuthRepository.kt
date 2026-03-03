package com.example.tutorial.data.repository

import com.example.tutorial.com.example.tutorial.core.worker.LocalCacheCleaner
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cacheCleaner: LocalCacheCleaner
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        cacheCleaner.clear()
        auth.currentUser ?: error("Authentication failed")
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        age: Int,
        sex: String
    ): Result<FirebaseUser> = runCatching {

        auth.createUserWithEmailAndPassword(email, password).await()
        val user = auth.currentUser ?: error("No Firebase user after sign-up")
        val uid = user.uid
        val now = FieldValue.serverTimestamp()

        firestore.runBatch { b ->

            b.set(
                firestore.collection("users").document(uid),
                mapOf("createdAt" to now)
            )

            b.set(
                firestore.collection("users")
                    .document(uid)
                    .collection("profile")
                    .document("main"),
                mapOf(
                    "uid" to uid,
                    "email" to email,
                    "displayName" to name,
                    "age" to age,
                    "sex" to sex,
                    "createdAt" to now
                )
            )

        }.await()

        cacheCleaner.clear()
        auth.currentUser!!
    }

    suspend fun resetPassword(email: String): Result<Unit> =
        runCatching { auth.sendPasswordResetEmail(email).await() }

    suspend fun changePassword(
        currentPw: String,
        newPw: String
    ): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No user")
        val cred = EmailAuthProvider.getCredential(user.email!!, currentPw)
        user.reauthenticate(cred).await()
        user.updatePassword(newPw).await()
    }


    fun logout() {
        runBlocking { cacheCleaner.clear() }
        auth.signOut()
    }
}
