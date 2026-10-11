package com.example.reto2.data.repository

import java.util.Locale
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
                    "La cantidad debe ser un número válido mayor que cero."
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

        val deviation =
            abs(appliedRate - referenceRate) /
                    referenceRate * 100.0

        if (!deviation.isFinite()) {
            return Result.failure(
                IllegalArgumentException(
                    "No fue posible calcular la desviación de la tasa."
                )
            )
        }

        if (deviation > MAX_DEVIATION_PERCENT) {
            val deviationFormatted = String.format(
                Locale.US,
                "%.2f",
                deviation
            )

            return Result.failure(
                IllegalArgumentException(
                    "Operación rechazada: la desviación es " +
                            "$deviationFormatted% y supera el límite del 2,5%."
                )
            )
        }

        val total = amount * appliedRate

        if (!total.isFinite() || total <= 0.0) {
            return Result.failure(
                IllegalArgumentException(
                    "El importe calculado no es válido."
                )
            )
        }

        return Result.success(total)
    }

}