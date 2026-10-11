package com.example.reto2.data.repository

import com.example.reto2.data.model.CurrencyOperation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OperationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // UID del administrador autorizado en Firebase.
    private val adminUid = "l3a0Sx1CEAQjnVQnOePl7t2t9242"

    /**
     * Registra una nueva operación pendiente de auditoría.
     */
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

            if (type !in listOf("BUY", "SELL")) {
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

            if (
                !amount.isFinite() ||
                !referenceRate.isFinite() ||
                !appliedRate.isFinite() ||
                amount <= 0.0 ||
                referenceRate <= 0.0 ||
                appliedRate <= 0.0
            ) {
                return Result.failure(
                    IllegalArgumentException(
                        "La cantidad y las tasas deben ser números positivos válidos."
                    )
                )
            }

            val validation = OperationValidator.validate(
                amount = amount,
                referenceRate = referenceRate,
                appliedRate = appliedRate
            )

            val total = validation.getOrThrow()

            if (!total.isFinite() || total <= 0.0) {
                return Result.failure(
                    IllegalArgumentException(
                        "El total calculado no es válido."
                    )
                )
            }

            val document = firestore
                .collection("operations")
                .document()

            val currentTime = System.currentTimeMillis()

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
                createdAt = currentTime,
                approvedAt = 0L
            )

            document.set(operation).await()

            Result.success(operation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Consulta las operaciones del usuario que inició sesión.
     */
    suspend fun getMyOperations(): Result<List<CurrencyOperation>> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(
                    IllegalStateException(
                        "Debes iniciar sesión para consultar tu historial."
                    )
                )

            val snapshot = firestore
                .collection("operations")
                .whereEqualTo("userId", user.uid)
                .get()
                .await()

            val operations = snapshot.documents
                .mapNotNull { document ->
                    document.toObject(CurrencyOperation::class.java)
                        ?.copy(id = document.id)
                }
                .sortedByDescending { it.createdAt }

            Result.success(operations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Consulta las operaciones pendientes de auditoría.
     * Solo puede acceder el administrador autorizado.
     */
    suspend fun getPendingAuditOperations():
            Result<List<CurrencyOperation>> {
        return try {
            checkAdmin()

            val snapshot = firestore
                .collection("operations")
                .whereEqualTo("status", "PENDING_AUDIT")
                .get()
                .await()

            val operations = snapshot.documents
                .mapNotNull { document ->
                    document.toObject(CurrencyOperation::class.java)
                        ?.copy(id = document.id)
                }
                .sortedByDescending { it.createdAt }

            Result.success(operations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aprueba o rechaza una operación pendiente.
     *
     * Al aprobar:
     * - Cambia status a APPROVED.
     * - Guarda la fecha de aprobación en approvedAt.
     * - Conserva createdAt sin cambios.
     *
     * Al rechazar:
     * - Cambia status a REJECTED.
     * - Conserva las fechas existentes.
     */
    suspend fun updateOperationStatus(
        operationId: String,
        newStatus: String
    ): Result<Unit> {
        return try {
            checkAdmin()

            if (operationId.isBlank()) {
                return Result.failure(
                    IllegalArgumentException(
                        "El identificador de la operación no es válido."
                    )
                )
            }

            if (newStatus !in listOf("APPROVED", "REJECTED")) {
                return Result.failure(
                    IllegalArgumentException(
                        "El estado debe ser APPROVED o REJECTED."
                    )
                )
            }

            val document = firestore
                .collection("operations")
                .document(operationId)

            val snapshot = document.get().await()

            if (!snapshot.exists()) {
                return Result.failure(
                    IllegalStateException(
                        "La operación ya no existe."
                    )
                )
            }

            val currentStatus = snapshot.getString("status")

            if (currentStatus != "PENDING_AUDIT") {
                return Result.failure(
                    IllegalStateException(
                        "Esta operación ya fue revisada o no está pendiente."
                    )
                )
            }

            val updates = mutableMapOf<String, Any>(
                "status" to newStatus
            )

            if (newStatus == "APPROVED") {
                updates["approvedAt"] = System.currentTimeMillis()
            } else {
                // Si se rechaza, no se modifica la fecha de aprobación.
                // Si no existía, se mantiene en 0.
                val existingApprovedAt =
                    snapshot.getLong("approvedAt") ?: 0L

                updates["approvedAt"] = existingApprovedAt
            }

            document.update(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Comprueba que el usuario autenticado sea el administrador.
     */
    private fun checkAdmin() {
        val user = auth.currentUser
            ?: throw IllegalStateException(
                "Debes iniciar sesión para acceder a la auditoría."
            )

        if (user.uid != adminUid) {
            throw SecurityException(
                "No tienes permisos para realizar esta acción."
            )
        }
    }
}