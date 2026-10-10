
package com.example.reto2.data.repository

import com.example.reto2.data.model.UserProfile
import com.example.reto2.data.remote.FirebaseManager
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseManager.auth
    private val firestore = FirebaseManager.firestore

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            val firebaseUser = result.user
                ?: return Result.failure(
                    Exception("No se pudo crear el usuario.")
                )

            val profile = UserProfile(
                uid = firebaseUser.uid,
                email = email.trim(),
                fullName = fullName.trim(),
                role = "user"
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(profile)
                .await()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(
                email.trim(),
                password
            ).await()

            val firebaseUser = result.user
                ?: return Result.failure(
                    Exception("No se pudo iniciar sesión.")
                )

            val document = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val profile = document.toObject(UserProfile::class.java)
                ?: return Result.failure(
                    Exception("No se encontró el perfil del usuario.")
                )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
