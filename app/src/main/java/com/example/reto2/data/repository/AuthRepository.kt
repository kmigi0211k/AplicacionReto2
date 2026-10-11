
package com.example.reto2.data.repository

import android.util.Log
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
            val normalizedEmail = email.trim()

            val result = auth.createUserWithEmailAndPassword(
                normalizedEmail,
                password
            ).await()

            val firebaseUser = result.user
                ?: return Result.failure(
                    Exception("No se pudo crear el usuario.")
                )

            val profile = UserProfile(
                uid = firebaseUser.uid,
                email = normalizedEmail,
                fullName = fullName.trim(),
                role = "user"
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(profile)
                .await()

            Log.d("AuthRepository", "Perfil registrado: ${firebaseUser.uid}")

            Result.success(profile)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al registrar", e)
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

            Log.d("AuthRepository", "UID AUTH: ${firebaseUser.uid}")

            val document = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            Log.d("AuthRepository", "DOCUMENT ID: ${document.id}")
            Log.d("AuthRepository", "DOCUMENT EXISTS: ${document.exists()}")
            Log.d("AuthRepository", "DOCUMENT DATA: ${document.data}")

            if (!document.exists()) {
                return Result.failure(
                    Exception(
                        "No existe el perfil con el UID: ${firebaseUser.uid}"
                    )
                )
            }

            val profile = document.toObject(UserProfile::class.java)
                ?: return Result.failure(
                    Exception(
                        "El documento existe, pero no se pudo convertir a UserProfile."
                    )
                )

            Log.d("AuthRepository", "ROLE: ${profile.role}")

            Result.success(profile)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al iniciar sesión", e)
            Result.failure(
                Exception("Error al iniciar sesión: ${e.message}", e)
            )
        }
    }

    fun logout() {
        auth.signOut()
    }
}
