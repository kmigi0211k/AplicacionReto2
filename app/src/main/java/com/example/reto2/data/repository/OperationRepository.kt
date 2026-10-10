
package com.example.reto2.data.repository

import com.example.reto2.data.model.CurrencyOperation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OperationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerOperation(
        type: String,
        baseCurrency: String,
        targetCurrency: String,
        amount: Double,
        referenceRate: Double,
        appliedRate: Double
    ): Result<CurrencyOperation> {

        return try {
            val user = auth.currentUser
                ?: return Result.failure(
                    IllegalStateException(
                        "Debes iniciar sesión para registrar una operación."
                    )
                )

            if (type != "BUY" && type != "SELL") {
                return Result.failure(
                    IllegalArgumentException(
                        "El tipo debe ser BUY o SELL."
                    )
                )
            }

            if (baseCurrency.isBlank() || targetCurrency.isBlank()) {
                return Result.failure(
                    IllegalArgumentException(
                        "Debes seleccionar las monedas."
                    )
                )
            }

            if (baseCurrency == targetCurrency) {
                return Result.failure(
                    IllegalArgumentException(
                        "Debes seleccionar dos monedas diferentes."
                    )
                )
            }

            val validation = OperationValidator.validate(
                amount = amount,
                referenceRate = referenceRate,
                appliedRate = appliedRate
            )

            val total = validation.getOrThrow()

            val document = firestore
                .collection("operations")
                .document()

            val operation = CurrencyOperation(
                id = document.id,
                userId = user.uid,
                type = type,
                baseCurrency = baseCurrency,
                targetCurrency = targetCurrency,
                amount = amount,
                referenceRate = referenceRate,
                appliedRate = appliedRate,
                total = total,
                status = "PENDING_AUDIT",
                createdAt = System.currentTimeMillis()
            )

            document.set(operation).await()

            Result.success(operation)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

