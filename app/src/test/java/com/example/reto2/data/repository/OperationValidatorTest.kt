package com.example.reto2.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationValidatorTest {

    @Test
    fun rejectsRateDeviationAboveLimit() {
        val result = OperationValidator.validate(
            amount = 100.0,
            referenceRate = 4000.0,
            appliedRate = 4120.0,
        )

        assertTrue(
            "La operación debería rechazarse si la desviación supera el 2,5 %",
            result.isFailure,
        )
    }

    @Test
    fun acceptsRateDeviationWithinLimit() {
        val result = OperationValidator.validate(
            amount = 100.0,
            referenceRate = 4000.0,
            appliedRate = 4050.0,
        )

        assertTrue(
            "La operación debería aceptarse si la desviación está dentro del límite",
            result.isSuccess,
        )

        assertEquals(
            "El total calculado debería ser 405000",
            405000.0,
            result.getOrThrow(),
            0.001,
        )
    }

    @Test
    fun rejectsZeroAmount() {
        val result = OperationValidator.validate(
            amount = 0.0,
            referenceRate = 4000.0,
            appliedRate = 4000.0,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsNegativeAmount() {
        val result = OperationValidator.validate(
            amount = -100.0,
            referenceRate = 4000.0,
            appliedRate = 4000.0,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsZeroReferenceRate() {
        val result = OperationValidator.validate(
            amount = 100.0,
            referenceRate = 0.0,
            appliedRate = 4000.0,
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsZeroAppliedRate() {
        val result = OperationValidator.validate(
            amount = 100.0,
            referenceRate = 4000.0,
            appliedRate = 0.0,
        )

        assertTrue(result.isFailure)
    }
}
