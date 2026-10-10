
package com.example.reto2.data.repository

import kotlin.math.abs

object OperationValidator {

    private const val MAX_DEVIATION_PERCENT = 2.5

    fun validate(
        amount: Double,
        referenceRate: Double,
        appliedRate: Double
    ): Result<Double> {

        if (!amount.isFinite() || amount <= 0.0) {
            return Result.failure(
                IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
                )
            )
        }

        if (!referenceRate.isFinite() || referenceRate <= 0.0) {
            return Result.failure(
                IllegalArgumentException(
                    "La tasa de referencia no es válida."
                )
            )
        }

        if (!appliedRate.isFinite() || appliedRate <= 0.0) {
            return Result.failure(
                IllegalArgumentException(
                    "La tasa aplicada no es válida."
                )
            )
        }

        val deviation = (
                abs(appliedRate - referenceRate) /
                        referenceRate
                ) * 100.0

        if (deviation > MAX_DEVIATION_PERCENT) {
            return Result.failure(
                IllegalArgumentException(
                    "Operación rechazada: la desviación es " +
                            "%.2f%% y supera el límite del 2,5%%."
                                .format(deviation)
                )
            )
        }

        val total = amount * appliedRate

        if (!total.isFinite()) {
            return Result.failure(
                IllegalArgumentException(
                    "El importe calculado no es válido."
                )
            )
        }

        return Result.success(total)
    }
}
